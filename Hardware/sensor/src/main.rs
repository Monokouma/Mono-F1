mod config;

use esp_idf_svc::hal::adc::{attenuation::DB_11, oneshot::{config::AdcChannelConfig, AdcChannelDriver, AdcDriver}};
use esp_idf_svc::hal::peripherals::Peripherals;
use std::thread;
use std::time::Duration;
use esp_idf_svc::wifi::{EspWifi, ClientConfiguration, Configuration};
use esp_idf_svc::nvs::EspDefaultNvsPartition;
use esp_idf_svc::eventloop::EspSystemEventLoop;

fn main() {
    esp_idf_svc::sys::link_patches();
    esp_idf_svc::log::EspLogger::initialize_default();

    let peripherals = Peripherals::take().unwrap();

    let sys_loop = EspSystemEventLoop::take().unwrap();
    let nvs = EspDefaultNvsPartition::take().unwrap();

    let mut wifi = EspWifi::new(
        peripherals.modem,
        sys_loop,
        Some(nvs)
    ).unwrap();

    use esp_idf_svc::wifi::AuthMethod;

    let wifi_config = Configuration::Client(ClientConfiguration {
        ssid: config::WIFI_SSID.try_into().unwrap(),
        password: config::WIFI_PASSWORD.try_into().unwrap(),
        auth_method: AuthMethod::WPA2Personal,
        ..Default::default()
    });

    wifi.set_configuration(&wifi_config).unwrap();
    wifi.start().unwrap();
    wifi.connect().unwrap();

    log::info!("Connecté au WiFi !");


    let adc = AdcDriver::new(peripherals.adc1).unwrap();

    let config = AdcChannelConfig {

        attenuation: DB_11,
        ..Default::default()
    };
    let mut channel = AdcChannelDriver::new(&adc, peripherals.pins.gpio34, &config).unwrap();

    log::info!("LDR started !");

    let mut last_value: u16 = 0;

    loop {
        let value: u16 = adc.read(&mut channel).unwrap();

        if (value as i32 - last_value as i32).abs() > 100 {
            log::info!("Changement détecté: {} -> {}", last_value, value);
            last_value = value;
        }


        thread::sleep(Duration::from_millis(100));
    }
}


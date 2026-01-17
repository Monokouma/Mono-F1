mod config;

use esp_idf_svc::hal::adc::{attenuation::DB_11, oneshot::{config::AdcChannelConfig, AdcChannelDriver, AdcDriver}};
use esp_idf_svc::hal::peripherals::Peripherals;
use std::thread;
use std::time::Duration;
use esp_idf_svc::wifi::{EspWifi, ClientConfiguration, Configuration};
use esp_idf_svc::nvs::EspDefaultNvsPartition;
use esp_idf_svc::eventloop::EspSystemEventLoop;

use esp_idf_svc::http::client::{Configuration as ApiConfig, EspHttpConnection};
use embedded_svc::http::client::Client;

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

    match check_health() {
        Ok(_) => log::info!("Health check OK"),
        Err(e) => log::error!("Health check failed: {:?}", e),
    }

    let adc = AdcDriver::new(peripherals.adc1).unwrap();

    let config = AdcChannelConfig {

        attenuation: DB_11,
        ..Default::default()
    };
    let mut channel = AdcChannelDriver::new(&adc, peripherals.pins.gpio34, &config).unwrap();

    log::info!("LDR started !");

    loop {
        let value: u16 = adc.read(&mut channel).unwrap();

        log::info!("Luminosity: {}", value);

        thread::sleep(Duration::from_millis(100));
    }
}

fn check_health() -> anyhow::Result<()> {
    let config = ApiConfig::default();
    let mut client = Client::wrap(EspHttpConnection::new(&config)?);

    let response = client.get("http://192.168.1.20:8080/api/health")?.submit()?;

    log::info!("Status: {}", response.status());

    Ok(())
}


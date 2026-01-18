mod config;

use esp_idf_svc::hal::adc::{attenuation::DB_11, oneshot::{config::AdcChannelConfig, AdcChannelDriver, AdcDriver}};
use esp_idf_svc::hal::peripherals::Peripherals;
use std::thread;
use std::time::Duration;
use esp_idf_svc::wifi::{EspWifi, ClientConfiguration, Configuration, AuthMethod};
use esp_idf_svc::nvs::EspDefaultNvsPartition;
use esp_idf_svc::eventloop::EspSystemEventLoop;
use embedded_svc::ws::FrameType;
use esp_idf_svc::ws::client::{EspWebSocketClient, EspWebSocketClientConfig, WebSocketEventType};

fn main() {
    esp_idf_svc::sys::link_patches();
    esp_idf_svc::log::EspLogger::initialize_default();

    let peripherals = Peripherals::take().unwrap();
    let sys_loop = EspSystemEventLoop::take().unwrap();
    let nvs = EspDefaultNvsPartition::take().unwrap();

    let mut wifi = EspWifi::new(peripherals.modem, sys_loop, Some(nvs)).unwrap();

    let wifi_config = Configuration::Client(ClientConfiguration {
        ssid: config::WIFI_SSID.try_into().unwrap(),
        password: config::WIFI_PASSWORD.try_into().unwrap(),
        auth_method: AuthMethod::WPA2Personal,
        ..Default::default()
    });

    wifi.set_configuration(&wifi_config).unwrap();
    wifi.start().unwrap();
    wifi.connect().unwrap();

    while !wifi.is_connected().unwrap() {
        thread::sleep(Duration::from_millis(100));
    }
    log::info!("Connected to WiFi !");

    let ws_config = EspWebSocketClientConfig {
        ..Default::default()
    };

    let mut ws = EspWebSocketClient::new(
        config::SERVER_URL,
        &ws_config,
        Duration::from_secs(10),
        |event| {
            if let Ok(event) = event {
                match event.event_type {
                    WebSocketEventType::Connected => log::info!("WS connected"),
                    WebSocketEventType::Disconnected => log::info!("WS disconnected"),
                    WebSocketEventType::Text(text) => log::info!("Received: {}", text),
                    _ => {}
                }
            }
        }
    ).unwrap();

    log::info!("WebSocket initialized.");

    let adc = AdcDriver::new(peripherals.adc1).unwrap();
    let adc_config = AdcChannelConfig {
        attenuation: DB_11,
        ..Default::default()
    };
    let mut channel = AdcChannelDriver::new(&adc, peripherals.pins.gpio34, &adc_config).unwrap();

    let mut last_value: u16 = 0;

    loop {
        let value: u16 = adc.read(&mut channel).unwrap();

        if (value as i32 - last_value as i32).abs() > 10 {
            let json = format!(r#"{{"value":{}}}"#, value);
            ws.send(FrameType::Text(false), json.as_bytes()).ok();
            log::info!("Send: {}", json);
            last_value = value;
        }

        thread::sleep(Duration::from_millis(100));
    }
}
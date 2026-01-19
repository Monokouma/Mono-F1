mod config;

use esp_idf_svc::hal::gpio::PinDriver;
use esp_idf_svc::hal::peripherals::Peripherals;
use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;
use esp_idf_svc::wifi::{EspWifi, ClientConfiguration, Configuration, AuthMethod};
use esp_idf_svc::nvs::EspDefaultNvsPartition;
use esp_idf_svc::eventloop::EspSystemEventLoop;
use esp_idf_svc::ws::client::{EspWebSocketClient, EspWebSocketClientConfig, WebSocketEventType};

fn main() {
    esp_idf_svc::sys::link_patches();
    esp_idf_svc::log::EspLogger::initialize_default();

    let peripherals = Peripherals::take().unwrap();
    let sys_loop = EspSystemEventLoop::take().unwrap();
    let nvs = EspDefaultNvsPartition::take().unwrap();

    let led = Arc::new(Mutex::new(PinDriver::output(peripherals.pins.gpio25).unwrap()));

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

    thread::sleep(Duration::from_secs(2));

    let ws_config = EspWebSocketClientConfig::default();
    let led_clone = led.clone();

    let _ws = EspWebSocketClient::new(
        config::SERVER_URL,
        &ws_config,
        Duration::from_secs(10),
        move |event| {
            if let Ok(event) = event {
                match event.event_type {
                    WebSocketEventType::Connected => log::info!("WS connected"),
                    WebSocketEventType::Disconnected => log::info!("WS disconnected"),
                    WebSocketEventType::Text(text) => {
                        log::info!("Received: {}", text);
                        if text.contains("\"on\"") {
                            led_clone.lock().unwrap().set_high().ok();
                            log::info!("LED ON");
                        } else if text.contains("\"off\"") {
                            led_clone.lock().unwrap().set_low().ok();
                            log::info!("LED OFF");
                        }
                    }
                    _ => {}
                }
            }
        }
    ).unwrap();

    log::info!("WebSocket initialized.");

    loop {
        thread::sleep(Duration::from_millis(100));
    }
}
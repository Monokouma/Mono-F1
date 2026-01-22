mod config;

use esp_idf_svc::hal::peripherals::Peripherals;
use std::sync::{Arc, Mutex};
use std::thread;
use std::time::Duration;
use esp_idf_svc::wifi::{EspWifi, ClientConfiguration, Configuration, AuthMethod};
use esp_idf_svc::nvs::EspDefaultNvsPartition;
use esp_idf_svc::eventloop::EspSystemEventLoop;
use esp_idf_svc::ws::client::{EspWebSocketClient, EspWebSocketClientConfig, WebSocketEventType};
use serde::Deserialize;
use ws2812_esp32_rmt_driver::Ws2812Esp32RmtDriver;
use smart_leds::RGB8;

const NUM_LEDS: usize = 40;

#[derive(Deserialize, Debug)]
struct CarState {
    #[serde(rename = "isOn")]
    is_on: bool,
    color: String,
}

fn hex_to_rgb(hex: &str) -> RGB8 {
    let r = u8::from_str_radix(&hex[0..2], 16).unwrap_or(0);
    let g = u8::from_str_radix(&hex[2..4], 16).unwrap_or(0);
    let b = u8::from_str_radix(&hex[4..6], 16).unwrap_or(0);
    RGB8::new(r, g, b)
}

fn write_leds(driver: &mut Ws2812Esp32RmtDriver, color: RGB8, num_leds: usize) {
    let pixels: Vec<u8> = (0..num_leds)
        .flat_map(|_| [color.g, color.r, color.b])
        .collect();
    driver.write_blocking(pixels.into_iter()).ok();
}

fn main() {
    esp_idf_svc::sys::link_patches();
    esp_idf_svc::log::EspLogger::initialize_default();

    let peripherals = Peripherals::take().unwrap();
    let sys_loop = EspSystemEventLoop::take().unwrap();
    let nvs = EspDefaultNvsPartition::take().unwrap();

    let ws2812 = Arc::new(Mutex::new(
        Ws2812Esp32RmtDriver::new(peripherals.rmt.channel0, peripherals.pins.gpio18).unwrap()
    ));

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
    log::info!("Connected to WiFi!");

    thread::sleep(Duration::from_secs(2));

    let ws_config = EspWebSocketClientConfig::default();
    let ws2812_clone = ws2812.clone();

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

                        if let Ok(state) = serde_json::from_str::<CarState>(&text) {
                            let color = if state.is_on {
                                log::info!("LED ON - Color: {}", state.color);
                                hex_to_rgb(&state.color)
                            } else {
                                log::info!("LED OFF");
                                RGB8::new(0, 0, 0)
                            };

                            write_leds(&mut ws2812_clone.lock().unwrap(), color, NUM_LEDS);
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
#include "PubSubClient.h"
#include "RESTClient.h"
#include "WiFi.h"
#include "ArduinoJson.h"
#include "DHT.h"

const char* ssid = "Hase caló";
const char* password = "WtfQueCalo19.";
const char* mqtt_server = "192.168.245.161";

String clientId = "ESP32 test";
const char* mqtt_username = "admin";
const char* mqtt_password = "password";

// SENSOR
const char* temperatura_topic = "casa/despacho/temperatura";
#define DHTPIN 4 // Probar con pin 2 también
#define DHTTYPE DHT22
DHT dht(DHTPIN, DHTTYPE);
 
WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];
//String mqtt_client_id = "ESP32Cliente-" + String(ESP.getChipModel());
 
void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(115200);
  setup_wifi();
  client.setServer(mqtt_server, 1883);
  client.setCallback(callback);
  dht.begin();
}
 
void setup_wifi() {
 
  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);
 
  WiFi.begin(ssid, password);
 
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
 
  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}
 
void callback(char* topic, byte* payload, unsigned int length) {
  Serial.print("Message arrived [");
  Serial.print(topic);
  Serial.print("] ");
  for (int i = 0; i < length; i++) {
    Serial.print((char)payload[i]);
  }
  Serial.println();
 
  if ((char)payload[0] == '1') {
    digitalWrite(LED_BUILTIN, LOW);
  } else {
    digitalWrite(LED_BUILTIN, HIGH);  // Turn the LED off by making the voltage HIGH
  }
 
}
 
void reconnect() {

  bool connected = client.connect(clientId.c_str(), mqtt_username, mqtt_password);
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if (connected) {
      Serial.println("connected\n");
      client.publish("casa/despacho/temperatura", "Enviando el primer mensaje");
      client.subscribe("casa/despacho/luz");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}
 
void loop() {
 
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // Leer el sensor cada cierto tiempo
  static unsigned long lastTime = 0;
  unsigned long now = millis();
  if (now - lastTime > 5000) { // Leer y publicar cada 5 segundos
    lastTime = now;
    float t = dht.readTemperature(); // Lee la temperatura en grados Celsius
    Serial.print("La temperatura es: ");
    Serial.println(t);

    // Verificar si la lectura fue existosa
    if(!isnan(t)){
      // Creamos el mensaje JSON
      JsonDocument jsonDocument;
      jsonDocument["temperatura"] = round(t*10.0) / 10.0; // Redondear a 1 decimal;

      // Convertimos a cadena JSON
      char jsonBuffer[64];
      serializeJson(jsonDocument, jsonBuffer);

      // Publicamos el mensaje JSON
      client.publish(temperatura_topic, jsonBuffer);
      Serial.print("Publicado en: ");
      Serial.println(temperatura_topic);
      Serial.println(": ");
      Serial.println(jsonBuffer);

     } else {
      Serial.println("Error al leer el sensor DHT!");
     }
  }

  delay(100);
}
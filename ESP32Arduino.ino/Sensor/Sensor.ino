#include "PubSubClient.h"
#include "WiFi.h"
#include "ArduinoJson.h"
#include "DHT.h"
#include <HTTPClient.h>

//Para el sensor ultrasonido
#define TRIG_PIN 15
#define ECHO_PIN 2
long og_dist; // Para medir cuando pasa alguién

const char* ssid = "isaac-H81M-S2H";
const char* password = "12345678";
const char* server = "10.42.0.1";

//Datos EJ
struct {
  int sensor_id;
  char* name;
  char* type;
  int device_id;
} SENSOR = {2, "Cam1", "NoDef", 24};


struct {
  int group_id;
  char* name;
  char* mqtt_channel;
  int home_id;
  bool supressed;
} GROUP = {5, "Salon", "Salon2", 2, 0};
 
WiFiClient espClient;
HTTPClient http;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];

char sensData_url[256];
char setAlarm_url[256];
 
int state = 0;

void setup() {
  sprintf(sensData_url, "http://%s:8081/api/control/sensorData", server);
  sprintf(setAlarm_url, "http://%s:8081/api/control/setAlarms", server);

  pinMode(LED_BUILTIN, OUTPUT);
  Serial.begin(115200);
  setup_wifi();
  client.setServer(server, 1883);
  client.setCallback(callback);
  
  pinMode(TRIG_PIN, OUTPUT);
  pinMode(ECHO_PIN, INPUT);
  digitalWrite(TRIG_PIN, LOW);
  og_dist = read_distance();
}
 
void setup_wifi() {
 
  delay(10);
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

  bool connected = client.connect("TEST ESP32");
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if (connected) {
      Serial.println("connected\n");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      delay(5000);
    }
  }
}
 
long read_distance(){
  long t; //timepo que demora en llegar el eco
 
  digitalWrite(TRIG_PIN, HIGH);
  delayMicroseconds(10);          //Enviamos un pulso de 10us
  digitalWrite(TRIG_PIN, LOW);
  
  t = pulseIn(ECHO_PIN, HIGH); //obtenemos el ancho del pulso
  return t/59;  // Distancia en cm
}



void loop() {
 
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

  // Leer el sensor cada cierto tiempo
  static unsigned long lastTime = 0;
  static unsigned long timer = 0;
  unsigned long now = millis();
  if (now - lastTime > 5000) { // Leer y publicar cada 5 segundos
    lastTime = now;
    float distancia = read_distance(); // Lee la temperatura en grados Celsius
    Serial.print("La distancia es: ");
    Serial.println(distancia);

    // Verificar si la lectura fue existosa
    if(!isnan(distancia)){

      // Creamos el mensaje JSON
      JsonDocument jsonDocument;
      jsonDocument["sensor_id"] = SENSOR.sensor_id;
      jsonDocument["valor"] = distancia; 

      // Convertimos a cadena JSON
      char jsonBuffer[64];
      serializeJson(jsonDocument, jsonBuffer);

      // Publicamos el mensaje JSON
      Serial.println(jsonBuffer);

      
      http.begin(sensData_url);
      http.addHeader("Content-Type", "application/json");

      int httpCode = http.POST(jsonBuffer);

      Serial.println(httpCode);

      if(distancia <= og_dist-5 && state == 0){
        Serial.println("Detect!");
        JsonDocument jsonDocument2;
        jsonDocument2["group_id"] = GROUP.group_id;
        jsonDocument2["alarm_state"] = "ON"; 

      // Convertimos a cadena JSON
        char jsonBuffer2[64];
        serializeJson(jsonDocument2, jsonBuffer2);

       // Publicamos el mensaje JSON
        Serial.println(jsonBuffer2);

      
        http.begin(setAlarm_url);
        http.addHeader("Content-Type", "application/json");

        int httpCode2 = http.POST(jsonBuffer2);

        state = 1;
      }else if(state == 4){
        Serial.println("Reset!");
        JsonDocument jsonDocument2;
        jsonDocument2["group_id"] = GROUP.group_id;
        jsonDocument2["alarm_state"] = "OFF"; 

      // Convertimos a cadena JSON
        char jsonBuffer2[64];
        serializeJson(jsonDocument2, jsonBuffer2);

       // Publicamos el mensaje JSON
        Serial.println(jsonBuffer2);

      
        http.begin(setAlarm_url);
        http.addHeader("Content-Type", "application/json");

        int httpCode2 = http.POST(jsonBuffer2);
        state = 0;
      }

      if(state >= 1)
        state++;


     } else {
      Serial.println("Error al leer el sensor DHT!");
     }


  }

  delay(100);
}
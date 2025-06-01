#include "PubSubClient.h"

#include <WiFi.h>
#include "ArduinoJson.h"

#define ALARM_PIN 15


//Configuracion red
const char* ssid = "isaac-H81M-S2H";
const char* password = "12345678";
const char* server = "10.42.0.1";

//Datos EJ
struct {
  int alarm_id;
  char* name;
  char* type;
  int device_id;
} ALARM = {3, "Alarm1", "NoDef", 25};

struct {
  int group_id;
  char* name;
  char* mqtt_channel;
  int home_id;
  bool supressed;
} GROUP = {5, "Salon", "Salon2", 2, 0};

int alarm_state = 0;
int alarm_control = 0;


String clientId = "ESP32 test";

WiFiClient espClient;
PubSubClient client(espClient);
long lastMsg = 0;
char msg[50];
//String mqtt_client_id = "ESP32Cliente-" + String(ESP.getChipModel());
char sensData_url[256];
char mqtt_topic[256];
 
void setup() {
  sprintf(sensData_url, "http://%s:8081/api/control/sensorData", server);
  sprintf(mqtt_topic, "%s/alarms/%d", GROUP.mqtt_channel,ALARM.alarm_id);

  pinMode(LED_BUILTIN, OUTPUT);
  pinMode(ALARM_PIN, OUTPUT);

  

  Serial.begin(115200);
  setup_wifi();
  client.setServer(server, 1883);
  client.setCallback(receive_alarm_command);
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
 
void receive_alarm_command(char *topic, byte *payload, unsigned int length){

  Serial.print("Comando de alarma recibido: ");

  String command = "";
  for (size_t i = 0; i < length; i++){
    command.concat((char)payload[i]);
  }
  
  Serial.print(command);
  Serial.println("");

  if(command == "ON"){
    alarm_state = 1;
  }else if(command == "OFF"){
    alarm_state = 0;
  }


}
 
void reconnect() {

  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    if (client.connect("ALARM ESP32")) {
      Serial.println("connected\n");
      bool sub = client.subscribe(mqtt_topic);
      Serial.print("Subscrito al topic: ");
      Serial.print(sub);
      Serial.println();
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

  //Control digital de la señal de la alarma
  if(alarm_state == 1){
    digitalWrite(ALARM_PIN, alarm_control);
    alarm_control = (alarm_control+1)%2;
  }else if(alarm_state == 0){
    digitalWrite(ALARM_PIN, LOW);
  }
  

  

  delay(500);
}
  

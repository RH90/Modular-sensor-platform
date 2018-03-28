#include <ESP8266WiFi.h>

//const char* ssid = "Tele2GatewayLZdR";//type your ssid
//const char* password = "C1C50999CA";//type your password
//const char* ssid = "MAPCI-fast-2.4GHz";//type your ssid
//const char* password = "mapcibjoern!";
const char* ssid = "ESP_test";//type your ssid
const char* password = "@T22413c";//type your password

char test=0;
int ledPin = 2; // GPIO2 of ESP8266
WiFiServer server(8800);//Service Port

void setup() {
Serial.begin(9600);
delay(10);

pinMode(ledPin, OUTPUT);
digitalWrite(ledPin, LOW);

// Connect to WiFi network
//Serial.println();
//Serial.println();


//Serial.println(ssid);

WiFi.begin(ssid, password);

while (WiFi.status() != WL_CONNECTED) {
delay(500);
}
 
// Start the server
server.begin();
Serial.print(WiFi.localIP());

}

void loop() {

// Check if a client has connected
WiFiClient client = server.available();
if (!client) {
return;
}
int a=0;
while(true){

if(client.available()>0){
  char r1=client.read();
  Serial.write(r1);
  Serial.flush();
  if(r1==1&&a==1){
    return;
  }
}
if(Serial.available()>0) {
  char r= Serial.read();
  client.write(r);
  if(r=='x'&&a==0){
    a==1;
  }
  if(r=='y'){
    client.flush();
  }
}
delayMicroseconds(1000);
  
}
//client.print(test);
}

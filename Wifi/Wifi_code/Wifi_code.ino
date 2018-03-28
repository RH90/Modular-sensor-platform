#include <ESP8266WiFi.h>

const char* ssid = "Tele2GatewayLZdR";//type your ssid
const char* password = "C1C50999CA";//type your password
char test=0;
int ledPin = 2; // GPIO2 of ESP8266
WiFiServer server(80);//Service Port

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

}

void loop() {

// Check if a client has connected
WiFiClient client = server.available();
if (!client) {
return;
}

//while(true){
  while(!client.available()){
delay(1);
}
byte b[1];
while(client.available()>0){
b[0]=client.read();
Serial.write(b,1);
Serial.flush();
delay(1);
}
//Serial.println("done");
while(true){

while(!Serial.available()) {
delay(1);
}  
//while(Serial.available()>0){
  char r= Serial.read();
  //Serial.print(Serial.read());
  if(r!='x'){
  client.write(r);
  client.flush();
  }else{
    client.write(r);
    client.flush();
    //delay(1);
    while(!client.available()){
    delay(1);
    }
    byte b1[1];
   // while(client.available()>0){
    b1[0]=client.read();
    Serial.write(b1,1);
    Serial.flush();
    //delay(1);
  //}
//}

//Serial.println(string);
}
}
//client.print(test);
}

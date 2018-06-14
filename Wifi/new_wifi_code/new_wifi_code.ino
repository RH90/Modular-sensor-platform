#include <ESP8266WiFi.h>

/*
 * new_wifi_code
 *
 * This code is used to make the wifi module to a transparent Wirless-Uart bridge
 * 
 * Created: 2018/02/27
 * @author Rilind Hasanaj <rilind.hasanaj0018@stud.hkr.se>
 */
const char* ssid = "Connectify-me";//type your ssid
const char* password = "1234qwer";//type your password

byte b[2048];
int i;

char test=0;
int ledPin = 2; // GPIO2 of ESP8266
WiFiServer server(8800);//Service Port

void setup() {
Serial.begin(9600); // start uart with the baudrate= 9600
delay(10);

pinMode(ledPin, OUTPUT);
digitalWrite(ledPin, LOW);

// Connect to WiFi network
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


while(true){

// wait in this while loop until data comes from TCP connection or from UART
while(client.available()<=0&&Serial.available()<=0)
{
  // return to top of the loop if connection is broken
  if(!client.connected())
  {
    return;
  }
  delay(1);
}

// while there is data from TCP connection 
while(client.available()>0){
  Serial.write(client.read()); // send data from TCP to UART
  delay(1);
}
Serial.flush();
delay(1);

// while there is data from UART connection

if(Serial.available()>0)
{
  i=0;
while(Serial.available()>0) {
  b[i] = Serial.read(); // send data from UART to TCP
  i++;
  delay(1);
}
    client.write(b,i);
    client.flush();
}

  
}

}

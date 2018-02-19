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
Serial.print(".");
}
Serial.println("");
Serial.println("WiFi connected");

// Start the server
server.begin();
Serial.println("Server started");

// Print the IP address
Serial.print("Use this URL to connect: ");
Serial.print("http://");
Serial.print(WiFi.localIP());
Serial.println("/");
}

void loop() {
// Check if a client has connected
WiFiClient client = server.available();
if (!client) {
return;
}

// Wait until the client sends some data

while(!Serial.available()) 
{  
  
}

while((test = Serial.read())!='a')
{
  client.print(test);
}
//Serial.println(string);


//client.print(test);
}

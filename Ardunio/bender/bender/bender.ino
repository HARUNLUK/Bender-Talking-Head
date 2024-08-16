#include <SoftwareSerial.h>
#include "SimpleSDAudio.h"

SoftwareSerial BTSerial(6, 7);  // RX, TX

// Defined file names
String welcomeFiles[] = { "0015", "0087","0040" };
const int numWelcomeFiles = sizeof(welcomeFiles) / sizeof(welcomeFiles[0]);

String idleFiles[] = { "0086", "0070", "0074", "0058", "0004", "0059", "0024" };
const int numIdleFiles = sizeof(idleFiles) / sizeof(idleFiles[0]);

unsigned long previousMillis = 0;
const long interval = 100000;  // Play idle sound every 100 seconds

bool btConnected = false;

void setup() {
  delay(1000);
  Serial.begin(9600);
  Serial.println("Enter AT Commands");
  BTSerial.begin(9600);

  SdPlay.setSDCSPin(4);  // SD Card CS Pin

  if (!SdPlay.init(SSDA_MODE_FULLRATE | SSDA_MODE_MONO | SSDA_MODE_AUTOWORKER)) {
    Serial.println("SD card initialization failed!");
    while (1);  // Stop on error
  }

  playRandomFile(welcomeFiles, numWelcomeFiles);  // Play a random welcome message at startup
}

void loop() {
  unsigned long currentMillis = millis();

  if (BTSerial.available()) {
    String fileName = BTSerial.readStringUntil('\n');  // Read the file name from Bluetooth
    fileName.trim();                                   // Remove leading and trailing spaces

    playFile(fileName);
    btConnected = true;
    Serial.println("Bluetooth Connected");
  }

  if (currentMillis - previousMillis >= interval && !btConnected) {

    playRandomFile(idleFiles, numIdleFiles);  // Play a random idle sound periodically
    previousMillis = currentMillis;
  }

  if (BTSerial.available() == 0 && btConnected) {
    btConnected = false;
    Serial.println("Bluetooth Disconnected");
    // No disconnection sound will be played
  }
}

void playFile(String fileName) {
  if (fileName.length() > 0) {
    fileName += ".wav";  // Append ".wav" to the file name
    Serial.print("Playing file: ");
    Serial.println(fileName);

    if (!SdPlay.setFile(fileName.c_str())) {
      Serial.print("File not found: ");
      Serial.println(fileName);

      if(7<random(10)){
        playFile("0088");
      }
      
    } else {
      SdPlay.play();
    }
  }
}

void playRandomFile(const String files[], int numFiles) {
  randomSeed(analogRead(9));
  int index = random(numFiles);  // Select a random file
  String fileName = files[index];
  playFile(fileName);  // Play the randomly selected file
}

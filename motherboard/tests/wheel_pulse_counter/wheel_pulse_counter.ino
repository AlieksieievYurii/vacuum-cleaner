#define INTERVAL 1000

#define HALL_ENCODER 2

volatile long wheel_pulse_count = 0;

void setup() {
  Serial.begin(9600);

  pinMode(HALL_ENCODER, INPUT);

  attachInterrupt(digitalPinToInterrupt(HALL_ENCODER), right_wheel_pulse, RISING);
}

void loop() {
  static uint32_t previous_milis = 0;

  if (millis() - previous_milis > INTERVAL) {
    Serial.print("Pulses: ");
    Serial.println(wheel_pulse_count);
    previous_milis = millis();
  }
}

void right_wheel_pulse() {
  wheel_pulse_count++;
}

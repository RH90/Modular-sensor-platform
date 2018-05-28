/*
 * main.c
 *
 * Created: 2018-05-14 14:07:50
 *  Author: Rilind
 * 
 *  This is the main code for the AVR micro controller, here it will receive Sensor data through
 *  SPI, I2c or analog  
 */ 

#include <avr/io.h>
#define F_CPU 1000000UL
#include <util/delay.h>
#include <avr/sfr_defs.h>
#include <avr/interrupt.h>
#include <stdlib.h>
// 16 MHz oscillator.

volatile int count=0;

volatile uint8_t A1=0x80;
volatile uint8_t A2=0x80;
volatile uint8_t A3;
volatile uint8_t A4;
volatile uint8_t A5;
volatile uint8_t A6;
volatile uint8_t TX_LED=0;
volatile uint8_t RX_LED=0;
volatile uint8_t pass=0xFF;
struct I2C_struct{
	uint8_t volatile addr;
	uint8_t volatile R_size;
	uint8_t volatile R_reg[6];
	uint8_t volatile W_size;
	uint8_t volatile W_reg;
	uint8_t volatile W_data;
};
struct SPI_struct{
	uint8_t volatile R_size;
	uint8_t volatile R_reg[6];
	uint8_t volatile W_size;
	uint8_t volatile W_reg;
	uint8_t volatile W_data;
};

struct I2C_struct I2C[2];
struct SPI_struct SPI[2];
volatile uint8_t SPI1_reg;
volatile uint8_t SPI2_reg;

volatile uint16_t delay=5;
volatile uint16_t count_delay=1;

// Initilize the ADC
// Initialize the Timmer1 with a interrupt every 100 ms
void Timer1init() {
	TIMSK1 = _BV(OCIE1A);  // Enable Interrupt TimerCounter0 Compare Match A (SIG_OUTPUT_COMPARE0A)
	//  TCCR1A = _BV(WGM11);  // Mode = CTC
	//TCCR1B = _BV(CS12) | _BV(CS10)|_BV(WGM12);   // Clock/1024, 0.001024 seconds per tick
	TCCR1B = _BV(CS12)|_BV(WGM12); //OC: 256
	OCR1A = 62500/16;
	// 6250
	// 3125=0.2 s
	sei();
}




int main (void)
{
	//asm("cli");  // DISABLE global interrupts.
	//DDRA|=0xC0;
	DDRA=0xFF;
	Timer1init();
	while(1) // main loop
	{
		//PORTA=0x80;
		//_delay_ms(1000);
		//PORTA=0;
		//_delay_ms(1000);
		//;;
	}
	
	return 0;
}



// the Timmer interrupt
ISR(TIMER1_COMPA_vect)
{
	A1^=0x80;
	PORTA=A1;
	
}
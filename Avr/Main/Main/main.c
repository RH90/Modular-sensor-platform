#include <avr/io.h>
#define F_CPU 16000000UL
#include <util/delay.h>
#include <avr/sfr_defs.h>
#include <avr/interrupt.h>
#include "i2cmaster.h"
#include "UART.h"
//#include <avr/interrupt.h>
  // 16 MHz oscillator.

volatile int count=0;

void print(int num,char c){
	char string[16];
	int j=0;
	for(;j<10;j++){
		string[j]=(num%10)+'0';
		serialWrite((num%10)+'0');
		num=num/10;
		if(num==0)
		break;
	}
	//int i=j;
	//char string1[j];
	//for(;i>=0;i--){
	//	serialWrite(string[i]);
	//}
	serialWrite(c);
}

void adc_init()
{
	// AREF = AVcc
	ADMUX = (1<<REFS0);
	
	// ADC Enable and pre scaler of 128
	// 16000000/128 = 125000
	ADCSRA = (1<<ADEN)|(1<<ADPS2)|(1<<ADPS1)|(1<<ADPS0);
}
void Timer1init() {
	TIMSK1 = _BV(OCIE1A);  // Enable Interrupt TimerCounter0 Compare Match A (SIG_OUTPUT_COMPARE0A)
	//  TCCR1A = _BV(WGM11);  // Mode = CTC
	TCCR1B = _BV(CS12) | _BV(CS10)|_BV(WGM12);   // Clock/1024, 0.001024 seconds per tick
	OCR1A = 15625;
	// 3125=0.2 s
	sei();
}
uint16_t adc_read(uint8_t ch)
{
	// select the corresponding channel 0~7
	// ANDing with ’7? will always keep the value
	// of ‘ch’ between 0 and 7
	ch &= 0b00000111;  // AND operation with 7
	ADMUX = (ADMUX & 0xF8)|ch; // clears the bottom 3 bits before ORing
	
	// start single conversion
	// write ’1? to ADSC
	ADCSRA |= (1<<ADSC);
	
	// wait for conversion to complete
	// ADSC becomes ’0? again
	// till then, run loop continuously
	while(ADCSRA & (1<<ADSC));
	
	return (ADC);
}
void session_init(){
	serialWrite('  ');
	serialWrite(serialRead());
	serialWrite('  ');
	_delay_ms(5000);
}


int main (void)
{
	//asm("cli");  // DISABLE global interrupts.
	adc_init();
	serial_init(MYUBRR);
	session_init();
	
	Timer1init();
	i2c_init();
	
	
	uint8_t* data;
	data = (uint8_t *)malloc(sizeof(uint8_t));
	_delay_ms(100);
	//dat = (uint8_t *)malloc(sizeof(uint8_t));
	data[0]=0x00;
	//i2c_writeReg(0x3C,0x20,data,1);
    data[0]=0x2F;
	i2c_writeReg(0x30,0x20,data,1);	
	DDRB =1;
	PORTB =1;
	while(1) // main loop
	{	// Send 'Hello' to the LCD

	;;		
	} //End main loop.
	
	return 0;
}
void Analog_digital_sensor(uint16_t pin_nmr,uint16_t method)
{


	
}
void I2C_sensor(uint16_t addr,uint16_t read_reg)
{

	
}
void SPI_sensor()
{
	
	

}
	


ISR(TIMER1_COMPA_vect)
{
		DDRB=1;
		PORTB=0;
		_delay_us(2);
		PORTB=1;
		_delay_us(5);
		PORTB=0;

		
		DDRB=0;
		int counter=0;

		while(!(PINB&0x01)&&counter<1000)
		{
			_delay_us(1);
			counter++;
		}
		int distance=0;
		while((PINB&0x01)&&distance<25000)
		{
			_delay_us(10);
			distance++;
		}
		
		distance =(float)distance *(float)0.174;
		
		print(distance,'a');
		print(adc_read(3),'b');
		
		uint8_t* dat;
		dat = (uint8_t *)malloc(sizeof(uint8_t));
		i2c_readReg(0x30,0x29,dat,1);
		//serialWrite(data[0]);
		print(*dat,'c');
		free(dat);
		
}




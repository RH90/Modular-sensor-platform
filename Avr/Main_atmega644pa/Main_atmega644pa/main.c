#include <avr/io.h>
#define F_CPU 16000000UL
#include <util/delay.h>
#include <avr/sfr_defs.h>
#include <avr/interrupt.h>
#include <stdlib.h>
#include "i2cmaster.h"
#include "UART.h"
//#include <avr/interrupt.h>
// 16 MHz oscillator.

volatile int count=0;

volatile uint8_t A1;
volatile uint8_t A2;
volatile uint8_t A3;
volatile uint8_t A4;
volatile uint8_t A5;
volatile uint8_t A6;
struct I2C_struct{
	 uint8_t volatile addr;
	 uint8_t volatile R_reg;
	 uint8_t volatile W_reg;
	 uint8_t volatile W_data;
	};

struct I2C_struct I2C[2];
volatile uint8_t SPI1_reg;
volatile uint8_t SPI2_reg;

volatile uint16_t delay=5;
volatile uint16_t count_delay=1;

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
	//TCCR1B = _BV(CS12) | _BV(CS10)|_BV(WGM12);   // Clock/1024, 0.001024 seconds per tick
	TCCR1B = _BV(CS12)|_BV(WGM12); //OC: 256
	OCR1A = 6250;
	// 6250
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
void I2CW(uint8_t dev,uint8_t reg, uint8_t dat)
{
		uint8_t* data;
		data = (uint8_t *)malloc(sizeof(uint8_t));
		//dat = (uint8_t *)malloc(sizeof(uint8_t));
		data[0]=dat;
		i2c_writeReg(dev,reg,data,1);
		free(data);
	
}
// TODO!
void session_init(){
	
	delay=serialRead();
	if(delay<=0){
		delay=10;
	}
	A1=serialRead();
	A2=serialRead();
	A3=serialRead();
	A4=serialRead();
	A5=serialRead();
	A6=serialRead();
	
	int i=0;
	for (i;i<2;i++)
	{
		uint8_t read =serialRead();
			if(read){
				I2C[i].addr=read;
				I2C[i].W_reg=serialRead();
				I2C[i].W_data=serialRead();
				if(I2C[i].W_data){
					I2CW(I2C[i].addr,I2C[i].W_reg,I2C[i].W_data);
				}
				I2C[i].R_reg=serialRead();
			}

	}

	//read =serialRead();
	//read =serialRead();

}


int main (void)
{
	//asm("cli");  // DISABLE global interrupts.
	adc_init();
	serial_init(MYUBRR);
	i2c_init();
	session_init();
	Timer1init();
	while(1) // main loop
	{	// Send 'Hello' to the LCD

		;;		
	} //End main loop.
	
	return 0;
}
void Analog_digital_sensor(uint16_t pin_nmr,uint16_t method,char id)
{
	if(method==1){
		DDRA=DDRA|(1<<pin_nmr);
		PORTA=PORTA^(1<<pin_nmr);
		_delay_us(2);
		PORTA=PORTA|(1<<pin_nmr);
		_delay_us(5);
		PORTA=PORTA^(1<<pin_nmr);

		
		DDRA=DDRA^(1<<pin_nmr);
		int counter=0;

		while(!(PINA&(1<<pin_nmr))&&counter<1000)
		{
			_delay_us(1);
			counter++;
		}
		int distance=0;
		while((PINA&(1<<pin_nmr))&&distance<25000)
		{
			_delay_us(10);
			distance++;
		}
		
		distance =(float)distance *(float)0.174;
		
		print(distance,id);
	}
	else if(method==2){
		print(adc_read(pin_nmr),id);
	}else{
		
	}

	
}
void I2C_sensor(uint16_t addr,uint16_t read_reg,char id)
{
	uint8_t* dat;
	dat = (uint8_t *)malloc(sizeof(uint8_t));
	i2c_readReg(addr,read_reg,dat,1);
	//serialWrite(data[0]);
	print(*dat,id);
	free(dat);
	
}
void SPI_sensor()
{
	
	

}



ISR(TIMER1_COMPA_vect)
{
	if (count_delay>=delay)
	{
		count_delay=1;
		Analog_digital_sensor(0,A1,'a');
		Analog_digital_sensor(1,A2,'b');
		Analog_digital_sensor(2,A3,'c');
		Analog_digital_sensor(3,A4,'d');
		//Analog_digital_sensor(4,A5,'e');
		//Analog_digital_sensor(1,A6,'f');
		if(I2C[0].addr)
		I2C_sensor(I2C[0].addr,I2C[0].R_reg,'g');
		if(I2C[1].addr)
		I2C_sensor(I2C[1].addr,I2C[1].R_reg,'h');
		serialWrite('x');
		if(serialRead()){
			session_init();
		}
	} 
	else
	{
		count_delay++;
	}
	
	
}




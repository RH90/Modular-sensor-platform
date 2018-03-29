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

// Used for converting a number to a string and send it together with a character that 
// identifies what sensor the number belongs to.
void print(int num,char c){
	char string[16];
	int j=0;
	if(num<0)
	num=num*-1;
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
// Initilize the ADC
void adc_init()
{
	// AREF = AVcc
	ADMUX = (1<<REFS0);
	// ADC Enable and pre scaler of 128
	// 16000000/128 = 125000
	ADCSRA = (1<<ADEN)|(1<<ADPS2)|(1<<ADPS1)|(1<<ADPS0);
}
// Initialize the Timmer1 with a interrupt every 100 ms
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
// Send/recieve spi data
uint8_t spi_tranceiver (uint8_t data)
{
	// Load data into the buffer
	//PORTB&= (1<<pinnmr);
	SPDR = data;
	//Wait until transmission complete
	while(!(SPSR & (1<<SPIF) ));
	//data = SPDR;
	//PORTB|= (1<<pinnmr);
	// Return received data
	return(SPDR);
}
// Initilize SPI
void spi_init_master (void)
{
	// Set MOSI, SCK as Output
	DDRB |= (1<<7)|(1<<5)|(1<<4)|(1<<0)|(1<<1);
	DDRB &= ~(1<<6);
	PORTB|=(1<<4)|(1<<0)|(1<<1);
	
	// Enable SPI, Set as Master
	//Prescaler: Fosc/16, Enable Interrupts (1<<SPR0)
	SPCR |= (1<<SPE)|(1<<MSTR);
}
// Used for reading and writing to SPI device
void ReadSPI(uint8_t reg,char c,uint8_t pin) {
	int temp;
	PORTB &= ~(1<<4);
	PORTB &= ~(1<<pin);
	spi_tranceiver(reg); // Call on register address for MSB temperature byte
	temp = spi_tranceiver(0xFF); // Exchange a garbage byte for the temperature byte
	PORTB |= (1<<4);
	PORTB |= (1<<pin);
	//return temp; // Return the 8 bit temperature
	print(temp,c);
}
// Reading Analog data from a specific pin and convert it to digital data 
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
// Function for writing to I2C device
void I2CW(uint8_t dev,uint8_t reg, uint8_t dat)
{
	uint8_t* data;
	data = (uint8_t *)malloc(sizeof(uint8_t));
	//dat = (uint8_t *)malloc(sizeof(uint8_t));
	data[0]=dat;
	i2c_writeReg(dev,reg,data,1);
	free(data);
	
}
// Had problems with reading a full byte from the Java program, therefor we read a byte 4 bits a time 
// and then shift it and combine it to a single byte and return it.
uint8_t read_pair()
{
	uint8_t a=serialRead();
	uint8_t b=serialRead();
	uint8_t c=a|(b<<4);	
	return c;
}
// This function get what type of sensors are used from user
void session_init(){
	
	uint8_t junk =serialRead();
	while(junk!=0x01){
		junk=serialRead();
	}
	// get the delay from user
	delay=serialRead();
	//delay cannot be 0
	if(delay<=0){
		delay=10;
	}
	
	A1=read_pair();
	A2=read_pair();
	A3=read_pair();
	A4=read_pair();
	A5=read_pair();
	A6=read_pair();
	//print(A6,'c');
	//serialWrite('x');
	//_delay_ms(10000);
	
	
	int i=0;
	for (i;i<2;i++)
	{
		I2C[i].addr =read_pair();
		if(I2C[i].addr!=pass){
			I2C[i].R_size=serialRead();
			I2C[i].W_size=serialRead();
			int j=0;
			for (j;j<I2C[i].W_size;j++)
			{
				I2C[i].W_reg=read_pair();
				I2C[i].W_data=read_pair();
				I2CW(I2C[i].addr,I2C[i].W_reg,I2C[i].W_data);
			}
			int k=0;
			for (k;k<I2C[i].R_size;k++)
			{
				I2C[i].R_reg[k]=read_pair();
			}
		}
	}
	
	int j=0;
	for (j;j<2;j++)
	{
		SPI[j].R_reg[0]=read_pair();
		if(SPI[j].R_reg[0]!=pass)
		{
			SPI[j].R_size=serialRead();
			SPI[j].W_size=serialRead();
			int k=1;
			for (k;k<SPI[j].R_size;k++)
			{
				SPI[j].R_reg[k]=read_pair();
			}
			int l=0;
			for (l;l<SPI[j].W_size;l++)
			{
				PORTB &= ~(1<<4);
				PORTB &= ~(1<<j);
				spi_tranceiver(read_pair()); //7a Call on register address for MSB temperature byte
				spi_tranceiver(read_pair()); // Exchange a garbage byte for the temperature byte
				PORTB |= (1<<4);
				PORTB |= (1<<j);
			}
		}	
	}	
	//print(45,'c');
	//serialWrite(123);
	//serialWrite(456);
	//serialWrite('x');
	//read =serialRead();
	//read =serialRead();
	
}


int main (void)
{
	//asm("cli");  // DISABLE global interrupts.
	spi_init_master();
	adc_init();
	serial_init(MYUBRR);
	i2c_init();
	session_init();
	Timer1init();
	while(1) // main loop
	{	
		;;		
	} 
	
	return 0;
}
// Read data from Analog or digital device
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
// reading data from I2C device
void I2C_sensor(uint8_t addr,uint8_t read_reg,char id)
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
// the Timmer interrupt
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
		int l=0;
		for (l;l<2;l++)
		{
			if(I2C[l].addr!=pass){
				int h=0;
				for (h;h<I2C[l].R_size;h++)
				{
					//I2C[0].R_reg[h]
					if(l==0)
					I2C_sensor(I2C[l].addr,I2C[l].R_reg[h],'g');
					if(l==1)
					I2C_sensor(I2C[l].addr,I2C[l].R_reg[h],'h');
				}
			}
		}
		int h=0;
		for (h;h<2;h++)
		{
			if(SPI[h].R_reg[0]!=pass){
				int v=0;
				for (v;v<SPI[h].R_size;v++)
				{
					//I2C[0].R_reg[h]
					if(h==0)
					ReadSPI(SPI[h].R_reg[v],'i',h);
					if(h==1)
					ReadSPI(SPI[h].R_reg[v],'j',h);
				}
			}
		}
		//this character tells the Java program that all sensors have been read
		serialWrite('y');
	} 
	else
	{
		count_delay++;
	}
	// This is used to tell if the micro controller should stop reading the sensors or not
	// when it sends the character 'x' the Java program will send 1 if the user want to pause the program
	// or 0 if it should not stop reading from sensors
	serialWrite('x');
	if(serialRead()){
		session_init();
	}
}




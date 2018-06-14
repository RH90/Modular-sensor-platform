/*
 *	main.c
 *
 *	Created: 2018-05-14 14:07:50
 *  Author: Rilind
 * 
 *  This is the main code for the AVR micro controller, here it will receive Sensor data through
 *  SPI, I2c or analog  
 */ 

#include <avr/io.h>
#define F_CPU 16000000UL // define clock to 16 MHZ
#include <util/delay.h>
#include <avr/sfr_defs.h>
#include <avr/interrupt.h>
#include <stdlib.h>
#include "i2cmaster.h"
#include "UART.h"
#include "SPI.h"

// variable for analog sensor nodes 
volatile uint8_t A1;
volatile uint8_t A2;
volatile uint8_t A3;
volatile uint8_t A4;
volatile uint8_t A5;
volatile uint8_t A6;

// variables for TX and RX LED
volatile uint8_t TX_LED=0;
volatile uint8_t RX_LED=0;

volatile uint8_t pass=0xFF;

// struct for I2C sensor
struct I2C_struct{
	uint8_t volatile addr;
	uint8_t volatile R_size;
	uint8_t volatile R_reg[6];
	uint8_t volatile W_size;
	uint8_t volatile W_reg;
	uint8_t volatile W_data;
};

// struct for SPI sensor
struct SPI_struct{
	uint8_t volatile R_size;
	uint8_t volatile R_reg[6];
	uint8_t volatile W_size;
	uint8_t volatile W_reg;
	uint8_t volatile W_data;
};
// initialize 2 I2C struct and 2 SPI struct
struct I2C_struct I2C[2];
struct SPI_struct SPI[2];

// variables for delay
volatile uint32_t count=0;
volatile uint32_t delay=5;
volatile uint32_t delay_max=5;
volatile uint32_t count_delay=1;

void print(int num,char c);
void adc_init(void);
void Timer1_init(void);
uint16_t adc_read(uint8_t ch);
void I2CW(uint8_t dev,uint8_t reg, uint8_t dat);
void ReadSPI(uint8_t reg,char c,uint8_t pin);
uint8_t read_pair(void);
void session_init();
void Analog_digital_sensor(uint16_t pin_nmr,uint16_t method,char id);
void I2C_sensor(uint8_t addr,uint8_t read_reg,char id);

// Used for converting a number to a string and send it together with a character that
// identifies what sensor the number belongs to.
void print(int num,char c){
	//char string[16];
	int j=0;
	if(num<0)
	{
		num=num*-1;
	}
	for(;j<10;j++){
		//string[j]=(num%10)+'0';
		serialWrite((num%10)+'0');
		num=num/10;
		if(num==0)
		break;
	}
	serialWrite(c);
}
// Initialize the ADC
void adc_init()
{
	// AREF = AVcc
	ADMUX = (1<<REFS0);
	// ADC Enable and pre scaler of 128
	// 16000000/128 = 125000
	ADCSRA = (1<<ADEN)|(1<<ADPS2)|(1<<ADPS1)|(1<<ADPS0);
}

// Initialize the Timmer1 with a interrupt every 10 ms
void Timer1_init() {
	TIMSK1 = _BV(OCIE1A);			// Output Compare A Match Interrupt Enable
	TCCR1B = _BV(CS12)|_BV(WGM12);	// Select pre scaler: 256
	OCR1A = 625;					// Set the output compare to 625
	sei();							// Enable global interrupt
	
	/*
		Calculation:	1/(Clock/Pre_scaler/Compare_value)	= delay between each interrupt
						1/(16*10^6/256/625)					= 10 ms
	*/
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
	data[0]=dat;
	i2c_writeReg(dev,reg,data,1);
	free(data);
	
}
// Used for reading and writing to SPI device
void ReadSPI(uint8_t reg,char c,uint8_t pin) {
	int temp;
	PORTB &= ~(1<<4);
	PORTB &= ~(1<<pin);
	spi_tranceiver(reg);			// Call on register address for data
	temp = spi_tranceiver(0xFF);	// Exchange a garbage byte for the data
	PORTB |= (1<<4);
	PORTB |= (1<<pin);
	print(temp,c);
}

// Had problems with reading a full byte from the Java program, therefor we read a byte 4 bits a time
// and then shift it and combine it to a single byte.
uint8_t read_pair()
{
	uint8_t a=serialRead(); // Get LSB of data
	uint8_t b=serialRead();	// Get MSB of data
	uint8_t c=a|(b<<4);		// add the together
	return c;
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

// This function get what type of sensors are used from user
void session_init(){
	// Turn on the TX led to indicate that the micro controller is waiting for input from the user
	PORTA=0x40;
	
	// The micro controller receives data from Uart and it will stay in a while loop until it receives the value 0x01
	uint8_t junk =serialRead();
	while(junk!=0x01){
		junk=serialRead();
	}
	// Turn on the RX led to indicate that 
	PORTA=0x80;
	// get the delay from user
	delay=serialRead();
	//delay cannot be 0
	if(delay<=0){
		delay=10;
	}
	delay*=10;
	delay_max=delay*100;
	
	// read data for the analog/digital sensors connected to PortA, if the data is 0xFF than that means that no sensor is connected
	// on that node and the micro controller should skip reading.
	A1=read_pair();
	A2=read_pair();
	A3=read_pair();
	A4=read_pair();
	A5=read_pair();
	A6=read_pair();
	
	// receive address and data for I2C sensors
	int i;
	for (i=0;i<2;i++)
	{
		I2C[i].addr =read_pair();		// I2C address
		if(I2C[i].addr!=pass){			// if the address that was received is equal 0xFF, if it is then skip
			I2C[i].R_size=serialRead(); // The number of register to read from
			I2C[i].W_size=serialRead(); // The number of register to write to
			//for loop for writing data to register
			int j;
			for (j=0;j<I2C[i].W_size;j++)
			{
				I2C[i].W_reg=read_pair();						// Register to write to
				I2C[i].W_data=read_pair();						// Data to write to the register
				I2CW(I2C[i].addr,I2C[i].W_reg,I2C[i].W_data);	// write to register
			}
			int k;
			for (k=0;k<I2C[i].R_size;k++)
			{
				I2C[i].R_reg[k]=read_pair();  // Register to read from
			}
		}
	}
	// receive address and data for SPI sensors
	int j;
	for (j=0;j<2;j++)
	{
		SPI[j].R_reg[0]=read_pair();
		if(SPI[j].R_reg[0]!=pass)		// check if you should skip this node
		{
			SPI[j].R_size=serialRead(); // Read Register size
			SPI[j].W_size=serialRead();	// Write Register size
			int k;
			for (k=1;k<SPI[j].R_size;k++)
			{
				SPI[j].R_reg[k]=read_pair(); // Read register
			}
			//for loop for writing data to register
			int l;
			for (l=0;l<SPI[j].W_size;l++)
			{
				PORTB &= ~(1<<4);				// set the SS pin to LOW
				PORTB &= ~(1<<j);				// set the pin that is connected to CS on the sensor to LOW
				spi_tranceiver(read_pair());	// Write register
				spi_tranceiver(read_pair());	// Write Data
				PORTB |= (1<<4);				// set the SS pin to High
				PORTB |= (1<<j);				// set the pin that is connected to CS on the sensor to High
			}
		}
	}
	/*
	print(read_pair(),'j');
	serialWrite('x');
	_delay_ms(10000);
	*/
	
}


int main (void)
{
	DDRA|=0xC0;				// Make the pins that are connected to the LEDs to output
	spi_init();				// initialize SPI
	adc_init();				// initialize ADC
	i2c_init();				// initialize I2C
	serial_init(MYUBRR);	// initialize UART
	session_init();			// initialize Session
	Timer1_init();			// initialize Timer
	while(1) 
	{
		;;	
	}
	
	return 0;
}



// A timer interrupt that triggers every 10 milliseconds
ISR(TIMER1_COMPA_vect)
{
	// This if if statement checks if the counter is a multiple of the delay that the user has
	// chosen and then starts reading from the sensors and sends the data.   
	if (count_delay%delay==0)
	{
		// Reads from the analog/digital sensors that are connected to Port A on the micro controller
		Analog_digital_sensor(0,A1,'a');
		Analog_digital_sensor(1,A2,'b');
		Analog_digital_sensor(2,A3,'c');
		Analog_digital_sensor(3,A4,'d');
		Analog_digital_sensor(4,A5,'e');
		Analog_digital_sensor(5,A6,'f');
		
		// This for loop reads the sensor data from the sensors connected to the I2C line
		int l;
		for (l=0;l<2;l++)
		{
			if(I2C[l].addr!=pass){
				int h;
				for (h=0;h<I2C[l].R_size;h++)
				{
					//I2C[0].R_reg[h]
					if(l==0)
					I2C_sensor(I2C[l].addr,I2C[l].R_reg[h],'g');
					if(l==1)
					I2C_sensor(I2C[l].addr,I2C[l].R_reg[h],'h');
				}
			}
		}
		
		// This for loop reads the sensor data from the SPI sensors
		int h;
		for (h=0;h<2;h++)
		{
			if(SPI[h].R_reg[0]!=pass){
				int v;
				for (v=0;v<SPI[h].R_size;v++)
				{
					if(h==0) // pin 0 
					ReadSPI(SPI[h].R_reg[v],'i',h);
					if(h==1) // pin 1
					ReadSPI(SPI[h].R_reg[v],'j',h);
				}
			}
		}
		// This sends the character 'y', which tells Java program that all sensors have been read
		serialWrite('y');
		// Indicate that the TX led should blink 1 time
		TX_LED=1;
	}

	// Turn on the led
	if(TX_LED==1)
	{
		PORTA |= 0x80;
		TX_LED=2;
	}else if(TX_LED==2) // turn of the led after 10 milliseconds
	{
		PORTA &= 0x7F;
		TX_LED=0;
	}
	if(RX_LED==1) // turn off the RX led
	{
		PORTA &= 0xBF;
		RX_LED=0;
	}
	// This is used to tell if the micro controller should stop reading the sensors or not
	// when it sends the character 'x' the Java program will send 1 if the user want to pause the program
	// or 0 if it should not stop reading from sensors
	// This happens every second
	if(count_delay%100==0)
	{
		// turn on the RX led
		PORTA |= 0x40;
		RX_LED=1;
		
		serialWrite('x');
		if(serialRead()==0x01){
			count_delay=1;
			TX_LED=0;
			RX_LED=0;
			PORTA &= 0x3F;
			session_init();
		}
	}
	count_delay++;
	// reset the counter variable.
	if (count_delay>delay_max)
	{
		count_delay=1;
	}	
}
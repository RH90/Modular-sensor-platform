#include <avr/io.h>
#define F_CPU 16000000UL
#include <util/delay.h>
#include <avr/sfr_defs.h>
#include "i2cmaster.h"
//#include <avr/interrupt.h>
  // 16 MHz oscillator.
#define BaudRate 9600
#define MYUBRR (F_CPU / 16 / BaudRate ) - 1




unsigned char serialCheckTxReady(void)
{
	return( UCSR0A & _BV(UDRE0) ) ;  // nonzero if transmit register is ready to receive new data.
}


void serialWrite(int DataOut)
{
	while (serialCheckTxReady() == 0)  // while NOT ready to transmit
	{;;}
	UDR0 = DataOut;
}


unsigned char serialCheckRxComplete(void)
{
	return( UCSR0A & _BV(RXC0)) ;  // _BV(x) macro set bit x in a byte which is equivalent to 1<<x. nonzero if serial data is available to read.
}


unsigned char serialRead(void)
{
	while( !(UCSR0A & (1 << RXC0)) )
	;
	return UDR0;
}

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


void serial_init(unsigned int bittimer)
{
	/* Set the baud rate */
	UBRR0H = (unsigned char) (bittimer >> 8);
	UBRR0L = (unsigned char) bittimer;
	/* set the framing to 8N1 (8 data bits + 1 stop bit (default) */
	UCSR0C = (3 << UCSZ00);
	/* Enable receiver and transmitter */
	UCSR0B = (1 << RXEN0) | (1 << TXEN0);
	return;
}

int main (void)
{
	//asm("cli");  // DISABLE global interrupts.

	serial_init(MYUBRR);
	//serialWrite('H'); // Char : H
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
		
		
		uint8_t* dat;
		dat = (uint8_t *)malloc(sizeof(uint8_t));
		i2c_readReg(0x30,0x29,dat,1);
		//serialWrite(data[0]);
		print(*dat,'b');
		free(dat);
		_delay_ms(1000);
		
	} //End main loop.
	
	return 0;
}


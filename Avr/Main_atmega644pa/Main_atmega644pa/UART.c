/*
 * UART.c
 *
 * Created: 2018-03-08 21:32:57
 *  Author: Rilind
 */ 
#include <avr/io.h>
#include <avr/sfr_defs.h>
#include "i2cmaster.h"
unsigned char serialCheckTxReady(void)
{
	return( UCSR1A & _BV(UDRE1) ) ;  // nonzero if transmit register is ready to receive new data.
}


void serialWrite(int DataOut)
{
	while (serialCheckTxReady() == 0)  // while NOT ready to transmit
	{;;}
	//UDR0 = DataOut;
	UDR1 = DataOut;
}


unsigned char serialCheckRxComplete(void)
{
	return( UCSR1A & _BV(RXC1)) ;  // _BV(x) macro set bit x in a byte which is equivalent to 1<<x. nonzero if serial data is available to read.
}


unsigned char serialRead(void)
{
	while( !(UCSR1A & (1 << RXC1)) )
	;
	return UDR1;
}
void serial_init(unsigned int bittimer)
{
	/* Set the baud rate */
	UBRR1H = (unsigned char) (bittimer >> 8);
	UBRR1L = (unsigned char) bittimer;
	/* set the framing to 8N1 (8 data bits + 1 stop bit (default) */
	UCSR1C = (3 << UCSZ10);
	/* Enable receiver and transmitter */
	UCSR1B = (1 << RXEN1) | (1 << TXEN1);
	return;
}
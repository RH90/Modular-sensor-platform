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
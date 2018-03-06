/*
 * testt.c
 *
 * Created: 2018-03-06 13:11:54
 * Author : Rilind
 */ 
#define F_CPU 16000000L
#include <avr/io.h>
#include <avr/delay.h>

int main(void)
{
	DDRC=0xFF;
	PORTC=0xFF;
    /* Replace with your application code */
    while (1) 
    {
		PORTC=0;
		_delay_ms(1000);
		PORTC=0xFF;
		_delay_ms(1000);
    }
}


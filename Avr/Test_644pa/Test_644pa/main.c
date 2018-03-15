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
	DDRA=0xFF;
	PORTA=0xFF;
    /* Replace with your application code */
    while (1) 
    {
		PORTA=0;
		_delay_ms(1000);
		PORTA=0xFF;
		_delay_ms(1000);
    }
}




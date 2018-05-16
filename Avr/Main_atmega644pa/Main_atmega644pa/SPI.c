/*
 * SPI.c
 *
 * Created: 2018-05-14 14:07:50
 *  Author: Rilind
 * 
 *  This code is for initializing the SPI and communicating with SPI devices  
 */ 

#include <avr/io.h>
#include <avr/sfr_defs.h>

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
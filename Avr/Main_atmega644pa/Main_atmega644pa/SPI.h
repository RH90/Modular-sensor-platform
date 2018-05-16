/*
 * SPI.h
 *
 * Created: 2018-05-14 14:08:08
 *  Author: Rilind
 */ 


#ifndef SPI_H_
#define SPI_H_
uint8_t spi_tranceiver (uint8_t data);
void spi_init_master (void);
void ReadSPI(uint8_t reg,char c,uint8_t pin);




#endif /* SPI_H_ */
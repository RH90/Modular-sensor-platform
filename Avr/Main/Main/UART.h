/*
* UART.c
*
* Created: 2018-03-08 21:32:57
*  Author: Rilind
*/ 
#define BaudRate 9600
#define MYUBRR (F_CPU / 16 / BaudRate ) - 1
unsigned char serialCheckTxReady(void);
void serialWrite(int DataOut);
unsigned char serialCheckRxComplete(void);
unsigned char serialRead(void);
void serial_init(unsigned int bittimer);
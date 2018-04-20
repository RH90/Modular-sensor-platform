
#include <compiler_defs.h>
#include <SI_C8051F990_Register_Enums.h>                  // SFR declarations
#include "InitDevice.h"
#include "F99x_SMBus_Master.h"

U8 					SMB_DATA_IN;                        // Global holder for SMBus data
// All receive data is written here

U8 					SMB_DATA_OUT;
U8 					SMB_REG_OUT;
U8 					START_SMB;
U8 					RW_Reg; // Global holder for SMBus data.
// All transmit data is read from here

U8 					TARGET;     // Target SMBus slave address


volatile int32_t 	temp_scaled;
int32_t 			a;

volatile bit 		SMB_BUSY;                 // Software flag to indicate when the
// SMB_Read() or SMB_Write() functions
// have claimed the SMBus
int32_t gas_range;
volatile bit 		SMB_RW;                   // Software flag to indicate the
// direction of the current transfer

SBIT (SDA, SFR_P0, 0);                 // SMBus on P0.0
SBIT (SCL, SFR_P0, 1);                 // and P0.1

LOCATED_VARIABLE_NO_INIT (reserved, U8, SEG_XDATA, 0x0000);

//-----------------------------------------------------------------------------
// Function PROTOTYPES
//-----------------------------------------------------------------------------

void SMB_Write (void);
void SMB_Read (void);
void T0_Wait_ms (U8 ms);
void SMB_Write_Reg(U8 Addr,U8 Reg, U8 Dat);
U8 SMB_Read_Reg(U8 Addr, U8 Reg);
void UART_Init(void);
void UART_Send(char c);
void print(char* string,U32 num);
int8_t getTemp(void);
int8_t getHum(void);
int16_t getGas(void);


//-----------------------------------------------------------------------------
// SiLabs_Startup() Routine
// ----------------------------------------------------------------------------
// This function is called immediately after reset, before the initialization
// code is run in SILABS_STARTUP.A51 (which runs before main() ). This is a
// useful place to disable the watchdog timer, which is enable by default
// and may trigger before main() in some instances.
//-----------------------------------------------------------------------------
void SiLabs_Startup (void)
{
	// Disable the watchdog here
}

//-----------------------------------------------------------------------------
// main() Routine
// ----------------------------------------------------------------------------
void UART_Init(void)
{
	//baud rate=57600
	SCON0 = 0x50;  // Asynchronous mode, 8-bit data and 1-stop bit
	TMOD = 0x20;  //Timer1 in Mode2.
	// TH1 = 256 - (24500000UL)/(long)(32*12*baudrate); // Load timer value for baudrate generation
	TH1 = (0x2B << TH1_TH1__SHIFT);
	TCON |= (1<<6);      //Turn ON the timer for Baud rate generation
}

void UART_Send(char c)
{
	SBUF0 = c;      // Load the data to be transmitted
	while(SCON0_TI==0);   // Wait till the data is trasmitted
	SCON0_TI = 0;
}
void print(char* string,U32 num)
{
	char c=0;
	char s[10];
	int8_t j=0;
	int8_t i=0;
	U8 len = 0;
	while ((c=(*(string++))) != '\0') {
		UART_Send(c);
		len++;
	}
	for(;j<10;j++){
		*(string++)=(num%10)+'0';
		s[j]=((num%10)+'0');
		num=num/10;
		if(num==0)
		break;
	}
	for(i=(j);i>=0;i--)
	{
		UART_Send(s[i]);
	}
	UART_Send('\r');
	//UART_Send('\n');

}
U8 SMB_Read_Reg(U8 Addr, U8 Reg)
{

	RW_Reg=0;
	TARGET = Addr;
	SMB_REG_OUT = Reg;
	START_SMB=1;// Define next outgoing byte
	SMB_Write();                     // Initiate SMBus write
	START_SMB=1;
	TARGET = Addr|0x01;             // Target the F3xx/Si8250 Slave for next								   // SMBus transfer
	SMB_Read();
	return SMB_DATA_IN;

}

void SMB_Write_Reg(U8 Addr,U8 Reg, U8 Dat)
{
	RW_Reg=1;
	TARGET = Addr;             // Target the F3xx/Si8250 Slave for next
	START_SMB=1;
	SMB_DATA_OUT = Dat;
	SMB_REG_OUT = Reg;// SMBus transfer
	SMB_Write();

	while(SMB_BUSY){
		;;

	}

}
int8_t getTemp(void)
{
	int32_t 			t_fine;
	int32_t 			calc_result;
	uint16_t 			adc;
	int32_t 			var1;
	int32_t 			var2;
	int32_t 			var3;
	const U16 			par_t1=26487;
	const int16_t 		par_t2=26223;
	const int8_t  		par_t3=3;// Dummy variable counters
	adc=((uint16_t)(SMB_Read_Reg(0xEE,0x22))<<8)|((SMB_Read_Reg(0xEE,0x23)));
	var1 = ((int16_t)adc << 1) - ((int16_t)par_t1 << 1);
	var2 = (var1 *  (int32_t)par_t2) >> 11;
	var3 = ((var1 >> 1) * (var1 >> 1)) >> 12;
	var3 = ((var3) * ((int16_t)par_t3 << 4)) >> 14;
	t_fine =(var2 + var3);
	temp_scaled =(((t_fine * 5) + 128) >> 8);
	calc_result=temp_scaled/100;
	return calc_result;
}
int8_t getHum(void)
{
	int32_t 			calc_result;
	uint16_t 			adc;
	int32_t 			var1;
	int32_t 			var2;
	int32_t 			var3;
	int32_t 			var4;
	int32_t 			var5;
	int32_t 			var6;
	const uint16_t 		par_h1=10211;
	const uint16_t		par_h2=16611;
	const int8_t 		par_h3=0;
	const int8_t 		par_h4=45;
	const int8_t		par_h5=20;
	const uint8_t		par_h6=120;
	const int8_t		par_h7=156;

	adc=((uint16_t)SMB_Read_Reg(0xEE,0x25)<<8)|(SMB_Read_Reg(0xEE,0x26));

	var1 =  (int32_t)(((int32_t)adc) - ((int32_t) par_h1*16 ));
	//print("1: ",test);
	var2 = ((int32_t) par_h2
					* (((temp_scaled * (int32_t) par_h4) / ((int32_t) 100))
						+ (((temp_scaled * ((temp_scaled * (int32_t) par_h5) / ((int32_t) 100))) >> 6)
							/ ((int32_t) 100)) + (int32_t) (1 << 14))) >> 10;
	//print("2: ",hvar2);
	var3 = var1 * var2;
	//print("3: ",hvar3);
	var4 = (int32_t)(par_h6 << 7);
	//print("4: ",hvar4);
	var4 = ((var4) + ((temp_scaled * (int32_t) par_h7) / ((int32_t) 100))) >> 4;
	//print("4: ",hvar4);
	var5 = ((var3 >> 14) * (var3 >> 14)) >> 10;
	//print("5: ",hvar5);
	var6 = (var4 * var5) >> 1;
	//print("6: ",hvar6);
	calc_result = (((var3 + var6) >> 10) * ((int32_t) 1000)) >> 12;
	//print("c_h: ",calc_hum);


	if (calc_result > 100000) // Cap at 100%rH
		calc_result = 100000;
	else if (calc_result < 0)
		calc_result = 0;
	calc_result/=1000;

	//calc_hum= ((uint32_t)hum_adc*(uint32_t)100)/65535;
	return calc_result;

}
int16_t getGas(void)
{

		float 			value1;
		float 			value2;
	    float 			var1;
		float 			var2;
		float 			var3;

		int32_t range_sw_err=0;
		int32_t gas_res_adc=0;
		int32_t calc_gas_res;


			/**Look up table 2 for the possible gas range values */


		gas_res_adc=((uint16_t)SMB_Read_Reg(0xEE,0x2A)<<2)|(SMB_Read_Reg(0xEE,0x2B)>>6);
		print("gas_res_adc: ",gas_res_adc);
		gas_range =(uint16_t)SMB_Read_Reg(0xEE,0x2B)&0x0F;
		print("gas_range: ",gas_range);
		/**Look up table 1 for the possible gas range values */
		/**Look up table 2 for the possible gas range values */
		switch(gas_range)
		{
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 6:
		case 8:
		case 9:
		case 12:
		case 14:
		case 15:
			value1=0.0;
					break;
		case 5:
			value1=-1.0;
					break;
		case 7:
			value1=-0.8;
					break;
		case 10:
			value1=-0.2;
				    break;
		case 11:
			value1=-0.5;
					break;
		case 13:
			value1=-1.0;
					break;
		}
		switch(gas_range)
				{
				case 0:
				case 1:
				case 2:
				case 3:
				case 6:
				case 9:
				case 10:
				case 11:
				case 13:
				case 12:
				case 14:
				case 15:
					value2=0.0;
							break;
				case 4:
					value2=0.1;
					break;
				case 5:
					value2=0.7;
							break;
				case 7:
					value2=-0.8;
							break;
				case 8:
					value2=-0.1;
							break;
				}
			var1 = (1340.0f + (5.0f * 123));
			var2 = (var1) * (1.0f + value1/100.0f);
			var3 = 1.0f + (value2/100.0f);
			calc_gas_res = 1.0f / (float)(var3 * (0.000000125f) * (float)(1 << gas_range) * (((((float)gas_res_adc)
				- 512.0f)/var2) + 1.0f));


		return calc_gas_res;

}
uint8_t get_heat(void)
{
	int32_t 			var1;
	int32_t 			var2;
	int32_t 			var3;
	int32_t 			var4;
	int32_t 			var5;
	const U8  			par_g1=124;
	const U16 			par_g2=250855;
	const U8  			par_g3=318;// Dummy variable counters

	uint8_t heatr_res;
	int32_t heatr_res_x100;
	int16_t temp= temp_scaled/100;

	if (temp > 400) /* Cap temperature */
		temp = 400;

	//var1 = (((int32_t) amb_temp * par_g3) / 1000) * 256;
	var2 = (par_g1 + 784) * (((((par_g2 + 154009) * temp * 5) / 100) + 3276800) / 10);
	var3 = var1 + (var2 / 2);
	//var4 = (var3 / (res_heat_range + 4));
	//var5 = (131 * res_heat_val) + 65536;
	heatr_res_x100 = (int32_t) (((var4 / var5) - 250) * 34);
	heatr_res = (uint8_t) ((heatr_res_x100 + 50) / 100);

	return heatr_res;
}

int main (void)
{
	U8  i;
	//Enter default mode
	enter_DefaultMode_from_RESET();
	//printf("%d",0x22);
	// If slave is holding SDA low because of an improper SMBus reset or error
	while(!SDA)
	{
		// Provide clock pulses to allow the slave to advance out
		// of its current state. This will allow it to release SDA.
		XBR2 = 0x40;                     // Enable Crossbar
		SCL = 0;                         // Drive the clock low
		for(i = 0; i < 255; i++);        // Hold the clock low
		SCL = 1;                         // Release the clock
		while(!SCL);                     // Wait for open-drain
		// clock output to rise
		for(i = 0; i < 10; i++);         // Hold the clock high
		XBR2 = 0x00;                     // Disable Crossbar
	}

	enter_Mode2_from_DefaultMode();
	UART_Init();

	//SMB_Write_Reg(0x30,0x20,0x37);

	SMB_Write_Reg(0xEE,0xE0,0xB6);// reset
	SMB_Write_Reg(0xEE,0x72,0x01);// hum:1x
	SMB_Write_Reg(0xEE,0x74,0x25);// temp:1x, pressure:1x

	SMB_Write_Reg(0xEE,0x64,0x59); //100 ms

	//par_g1=SMB_Read_Reg(0xEE,0xED);
	//par_g2=(SMB_Read_Reg(0xEE,0xEC)<<8)|SMB_Read_Reg(0xEE,0xEB);
	//par_g3 =SMB_Read_Reg(0xEE,0xEE);

	//par_t1=(SMB_Read_Reg(0xEE,0xEA)<<8)|SMB_Read_Reg(0xEE,0xE9);
	//par_t2=(SMB_Read_Reg(0xEE,0x8B)<<8)|SMB_Read_Reg(0xEE,0x8A);
	//par_t3 =SMB_Read_Reg(0xEE,0x8C);

	//par_h1=(SMB_Read_Reg(0xEE,0xe3)<<8)|SMB_Read_Reg(0xEE,0xe2);
	//par_h2=(SMB_Read_Reg(0xEE,0xe1)<<8)|SMB_Read_Reg(0xEE,0xe2);
	//par_h3=SMB_Read_Reg(0xEE,0xe4);
	//par_h4=SMB_Read_Reg(0xEE,0xe5);
	//par_h5=SMB_Read_Reg(0xEE,0xe6);
	//par_h6=SMB_Read_Reg(0xEE,0xe7);
	//par_h7=SMB_Read_Reg(0xEE,0xe8);

	//SMB_Write_Reg(0xEE,0x64,0x59);// 100ms heatup

	while (1)
	{
		SMB_Write_Reg(0xEE,0x74,0x25);// trigger forced mode

		print("Temp: ",getTemp());
		print("Hum: ",getHum());
		getGas();
		print("--------",0);

		YELLOW_LED = !YELLOW_LED;

		for(a=0;a<100000;a++){
			;;

			// Wait 50 ms until the next cycle
		}

	}

	return 0;
}

void SMB_Write (void)
{
	while (SMB_BUSY);                   // Wait for SMBus to be free.
	SMB_BUSY = 1;                       // Claim SMBus (set to busy)
	SMB_RW = 0;                         // Mark this transfer as a WRITE
	SMB0CN_STA = 1;                            // Start transfer
}
void SMB_Read (void)
{
	while (SMB_BUSY != 0);               // Wait for transfer to complete
	SMB_BUSY = 1;                       // Claim SMBus (set to busy)
	SMB_RW = 1;                         // Mark this transfer as a READ

	SMB0CN_STA = 1;                            // Start transfer

	while (SMB_BUSY);                   // Wait for transfer to complete
}

void T0_Wait_ms (U8 ms)
{

	while (ms) {
		TCON_TR0 = 0;                         // Stop Timer0
		TH0 = ((-(SYSCLK/1000)) >> 8);   // Overflow in 1ms
		TL0 = ((-(SYSCLK/1000)) & 0xFF);
		TCON_TF0 = 0;                         // Clear overflow indicator
		TCON_TR0 = 1;                         // Start Timer0
		while (!TCON_TF0);                    // Wait for overflow
		ms--;                            // Update ms counter
	}

	TCON_TR0 = 0;                            // Stop Timer0
}

//-----------------------------------------------------------------------------
// End Of File
//-----------------------------------------------------------------------------

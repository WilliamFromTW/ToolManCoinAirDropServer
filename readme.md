### How To Compile    

1. j2se version 8 or above        
2. gradle 2.7 or above            
   gradle jar => generate jar file    
       
### StartUp     
1. Modify server.properties     
"PROP_SENDER_WALLET_PASSPHRASE"  your pass phrase        
"PROP_GAIN_PERIOD" gain 1 dollar period     
"PROP_TRANSFER_COIN_NUMBER" minimum number transfer to client wallet    
 
2. Execute
java -Xmx768M -cp .;./ToolManCoinAirDropServer-1.0b1.jar tmc.server.AirDropServer 
  

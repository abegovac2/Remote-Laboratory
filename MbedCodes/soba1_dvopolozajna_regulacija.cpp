//
// Ugradbeni sistemi
// SOBA1: Dvopoložajna regulacija temperature
//

#define SUBSETUP "project225883/us/etf/message/soba1/setup/mbed"
#define SUBNTCVOLTAGE "theme/ntcvoltage" //needed for simulating physical input of ntc voltage
#define SUBRUNNINGTIME "project225883/us/etf/message/soba1/mbed/timewant"

#define PUBSETUP "project225883/us/etf/message/soba1/setup/mbed"
#define PUBHEATERSTATE "project225883/us/etf/message/soba1/mbed/heater"
#define PUBRUNNINGTIME "project225883/us/etf/message/soba1/mbed/time"

#include "mbed.h"

#define MQTTCLIENT_QOS2 0

#include "easy-connect.h"
#include "MQTTNetwork.h"
#include "MQTTmbed.h"
#include "MQTTClient.h"
#include <string.h>

Ticker check30min;
Timer time_running;
DigitalOut led1(p7);
AnalogOut dc_dimmer(p18);
float v_ntc;
bool heater=false;
int block_of_30min=0;
bool mqqt_wants_time=false;
bool data_to_send=false;

void subsetup_fun(MQTT::MessageData& md){
    data_to_send=true;
}

void heater_regulation(MQTT::MessageData& md)
{
    /*
    temperatura=1/0.04799 * ln(2.131*10^4/((v_ntc*673)/(3.3-v_ntc)))
    look-up:
    23C -> Vntc = 3.01305 V
    25C -> Vntc = 2.98689 V
    27C -> Vntc = 2.95861 V
    */
    MQTT::Message &message = md.message;
    printf("Napon na NTC %.*s\r\n", message.payloadlen, (char*)message.payload);
    v_ntc=atof((char*)message.payload);
    printf("Stanje: ");

    if(v_ntc < 2.98689){ // > 25 C
        heater=false;
        printf("25 C+ | Grijac: OFF\n");
    }
    else if(v_ntc < 3.03722){ // [21 C, 25 C]
        heater=false;
        printf("23+-2 C | Grijac: OFF\n");
    }
    else{ // < 21 C
        heater=true;
        printf("21 C- | Grijac: ON\n");
    }
}

void wantTime(MQTT::MessageData& md){
    // kod
    mqqt_wants_time=true;
}

int running_time_in_minutes(){
    return block_of_30min*30+time_running.read()/60;
}

void check30min_fun(){
    time_running.stop();
    block_of_30min++;
    time_running.start();
}

int main(int argc, char* argv[])
{
    time_running.start();
    check30min.attach(&check30min_fun, 30*60);
    printf("Ugradbeni sistemi\r\n");
    printf("SOBA 1: Dvopoložajna regulacija\r\n\r\n");

    NetworkInterface *network;
    network = NetworkInterface::get_default_instance();
    
    if (!network) {
        return -1;
    }

    MQTTNetwork mqttNetwork(network);

    MQTT::Client<MQTTNetwork, Countdown> client(mqttNetwork);

    const char* hostname = "broker.hivemq.com";
    int port = 1883;
    printf("Connecting to %s:%d\r\n", hostname, port);
    int rc = mqttNetwork.connect(hostname, port);
    if (rc != 0)
        printf("rc from TCP connect is %d\r\n", rc);

    MQTTPacket_connectData data = MQTTPacket_connectData_initializer;
    data.MQTTVersion = 3;
    data.clientID.cstring = "proizvoljno";
    data.username.cstring = "";
    data.password.cstring = "";
    if ((rc = client.connect(data)) != 0)
        printf("rc from MQTT connect is %d\r\n", rc);

    if ((rc = client.subscribe(SUBNTCVOLTAGE, MQTT::QOS2, heater_regulation)) != 0)//
        printf("rc from MQTT subscribe is %d\r\n", rc);
        
    if ((rc = client.subscribe(SUBRUNNINGTIME, MQTT::QOS2, wantTime)) != 0)//
        printf("rc from MQTT subscribe is %d\r\n", rc);
    
    if ((rc = client.subscribe(SUBSETUP, MQTT::QOS2, subsetup_fun)) != 0)//
        printf("rc from MQTT subscribe is %d\r\n", rc);
        
    MQTT::Message message;

    // QoS 0
    char buf[100];
    bool old_heater=false;
    while(1) {
        led1 = heater; 
        if(heater) dc_dimmer=1; //output 3.3V for DC dimmer INPUT
        else dc_dimmer=0;
        
        if (old_heater!=heater) {
            old_heater=heater;
            sprintf(buf, "{\"Stanje\": \"Stanje grijaca: %d\"}", heater);
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBHEATERSTATE, message);
        }
        if(mqqt_wants_time){
            mqqt_wants_time=false;
            sprintf(buf, "{\"Stanje\": \"Sistem u pripravnosti: %d minuta\"}", running_time_in_minutes);
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBRUNNINGTIME, message);
        }
        if(data_to_send){
            data_to_send=false;
            sprintf(buf, "{\"Topics\": [\"heater\",\"time\"]}");
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBSETUP, message);
        }        
        
        rc = client.subscribe(SUBNTCVOLTAGE, MQTT::QOS0, heater_regulation);
        rc = client.subscribe(SUBRUNNINGTIME, MQTT::QOS0, wantTime);
        rc = client.subscribe(SUBSETUP, MQTT::QOS0, subsetup_fun);
        wait(1);
    }

}

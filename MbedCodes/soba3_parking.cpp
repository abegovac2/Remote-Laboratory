//
// Ugradbeni sistemi
// SOBA3: Parking
//

#define SUBSETUP "project225883/us/etf/message/soba3/mbed/info"
#define SUBPARKING "project225883/us/etf/message/soba3/mbed/portparking"
#define SUBXPARKING "project225883/us/etf/message/soba3/mbed/portexit"
#define SUBRUNNINGTIME "project225883/us/etf/message/soba3/mbed/porttimewant"

#define PUBSETUP "project225883/us/etf/message/soba3/mbed"
#define PUBPARKING "project225883/us/etf/message/soba3/mbed/parking"
#define PUBRUNNINGTIME "project225883/us/etf/message/soba3/mbed/time"

#include "mbed.h"

#define MQTTCLIENT_QOS2 0

#include "easy-connect.h"
#include "MQTTNetwork.h"
#include "MQTTmbed.h"
#include "MQTTClient.h"
#include <string.h>

Ticker check30min;
Timer time_running;
Timer park_place[20];
//Ticker check_30_min[20];
int park_place_taken[20];
int block_30_min[20];
int block_of_30min=0;

float ticket=0;
int info=-1, info2=-1;
int tmp_car=-1;
int time_in_min=0;

bool mqqt_wants_time=false;
bool data_to_send=false;

void subsetup_fun(MQTT::MessageData& md){
    data_to_send=true;
}

void reset_system(){
    time_in_min=0;
    info=-1;
    info2=-1;
    tmp_car=-1;
    ticket=0;
    for(int i=0;i<20;i++) park_place_taken[i]=0;
    for(int i=0;i<20;i++) block_30_min[i]=0;
    for(int i=0;i<20;i++) park_place[i].stop();
}


void park_action(MQTT::MessageData& md){
    MQTT::Message &message = md.message;
    printf("Usluga: %.*s\r\n", message.payloadlen, (char*)message.payload);
    int i;
    for(i=0;i<20;i++){
        if(park_place_taken[i]==0){
            park_place_taken[i]=1;
            park_place[i].start();
            //check_30_min[i].attach(&check_30_min_fun(i), 30*60);
            tmp_car=i;
            info=1;
            break;
        }
    }
    if(i==20) info=2; //no free space
}

void park_exit(MQTT::MessageData& md){
    MQTT::Message &message = md.message;
    printf("Usluga: %.*s\r\n", message.payloadlen, (char*)message.payload);
    int chosen_place=atoi((char*)message.payload);
    if(chosen_place>=0 && chosen_place<20 && park_place_taken[chosen_place]==1){
        //calculating the ticket price
        ticket = 0.02*(park_place[chosen_place].read()/60+block_30_min[chosen_place]*30);
        
        time_in_min=park_place[chosen_place].read()/60+block_30_min[chosen_place]*30;
        // deleting the data
        park_place[chosen_place].stop();
        block_30_min[chosen_place]=0;
        park_place_taken[chosen_place]=0;
        
        info2=1;//info for paying price
    }
}
/*

void check_30_min_fun(int place){
    park_place[place].stop();
    block_30_min[place]++;
    park_place[place].start();
}
*/

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
    reset_system();    
    printf("Ugradbeni sistemi\r\n");
    printf("SOBA 3: Parking\r\n\r\n");

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

    if ((rc = client.subscribe(SUBPARKING, MQTT::QOS2, park_action)) != 0)//
        printf("rc from MQTT subscribe is %d\r\n", rc);
        
    if ((rc = client.subscribe(SUBXPARKING, MQTT::QOS2, park_exit)) != 0)//
        printf("rc from MQTT subscribe is %d\r\n", rc);
    
    if ((rc = client.subscribe(SUBSETUP, MQTT::QOS2, subsetup_fun)) != 0)//
        printf("rc from MQTT subscribe is %d\r\n", rc);
        
    if ((rc = client.subscribe(SUBRUNNINGTIME, MQTT::QOS2, wantTime)) != 0)//
        printf("rc from MQTT subscribe is %d\r\n", rc);
        
    MQTT::Message message;

    // QoS 0
    char buf[100];
    while(1) {
        if(info!=-1){
            if(info==1) sprintf(buf, "{\"Message\": \"Zauzeli ste mjesto: %d\"}", tmp_car);
            else if(info==2) sprintf(buf, "{\"Message\": \"Nema slobodnog mjesta!\"}");
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBPARKING, message);
            info=-1, tmp_car=-1;
            printf("Auto zabiljezeno\n");
        }
        if(info2!=-1){
            sprintf(buf, "{\"Message\": \"Cijena parkinga: %.2f KM\"}",ticket); // Provedeno minuta: , ticket, time_in_min);
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBPARKING, message);
            info2=-1, time_in_min=0;
        }
        if(data_to_send){
            data_to_send=false;
            sprintf(buf, "{\"Message\": \"[mbed]: portexit, portparking, info\"}");
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBSETUP, message);
        } 
        if(mqqt_wants_time){
            mqqt_wants_time=false;
            sprintf(buf, "{\"Message\": \"[mbed]: %d minuta od zadnjeg restarta\"}", running_time_in_minutes());
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBRUNNINGTIME, message);
            printf("Poslan info o vremenu (%d)\n", running_time_in_minutes());
        }
        
        rc = client.subscribe(SUBPARKING, MQTT::QOS0, park_action);
        rc = client.subscribe(SUBXPARKING, MQTT::QOS0, park_exit);
        rc = client.subscribe(SUBSETUP, MQTT::QOS0, subsetup_fun);
        rc = client.subscribe(SUBRUNNINGTIME, MQTT::QOS0, wantTime);
        wait(1);
    }

}

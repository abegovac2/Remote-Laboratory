//
// Ugradbeni sistemi
// SOBA3: Parking
//

#define SUBSETUP "project225883/us/etf/message/soba3/setup/mbed"
#define SUBPARKING "project225883/us/etf/message/soba3/mbed/parking"
#define SUBXPARKING "project225883/us/etf/message/soba3/mbed/exit"

#define PUBSETUP "project225883/us/etf/message/soba3/setup/mbed"
#define PUBPARKING "project225883/us/etf/message/soba3/mbed/info"

#include "mbed.h"

#define MQTTCLIENT_QOS2 0

#include "easy-connect.h"
#include "MQTTNetwork.h"
#include "MQTTmbed.h"
#include "MQTTClient.h"
#include <string.h>

Timer park_place[20];
//Ticker check_30_min[20];
int park_place_taken[20];
int block_30_min[20];

float ticket=0;
int info=-1, info2=-1;
int tmp_car=-1;
int time_in_min=0;

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
    for(int i=0;i<20;i++) park_place_taken[i]=1;
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
int main(int argc, char* argv[])
{
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
        
    MQTT::Message message;

    // QoS 0
    char buf[100];
    while(1) {
        if(info!=-1){
            if(info==1) sprintf(buf, "{\"Zauzeli ste mjesto\": %d}", tmp_car);
            else if(info==2) sprintf(buf, "{\"Nema slobodnog mjesta!\"}");
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBPARKING, message);
            info=-1, tmp_car=-1;
        }
        if(info2!=-1){
            sprintf(buf, "{\"Cijena parkinga\": %.2f | Provedeno minuta: %d}", ticket, time_in_min);
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
            sprintf(buf, "{\"Topics\": [\"info\"]}");
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBSETUP, message);
        } 
        
        rc = client.subscribe(SUBPARKING, MQTT::QOS0, park_action);
        rc = client.subscribe(SUBXPARKING, MQTT::QOS0, park_exit);
        rc = client.subscribe(SUBSETUP, MQTT::QOS0, subsetup_fun);
        wait(1);
    }

}

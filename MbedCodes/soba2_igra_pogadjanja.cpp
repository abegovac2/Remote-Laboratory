//
// Ugradbeni sistemi
// SOBA2: Fun room
//

#define SUBSETUP "project225883/us/etf/message/soba2/setup/mbed"
#define SUBGUESS "project225883/us/etf/message/soba1/mbed/guess"

#define PUBSETUP "project225883/us/etf/message/soba2/setup/mbed"
#define PUBGAMEINFO "project225883/us/etf/message/soba1/mbed/info"

#include "mbed.h"
#define MQTTCLIENT_QOS2 0

#include "easy-connect.h"
#include "MQTTNetwork.h"
#include "MQTTmbed.h"
#include "MQTTClient.h"
#include <string.h>

bool gameon=true;
bool pause_game=false;

int winner=-1;
int playing=0;
int attempts[2]={0,0};
int hits[2]={0,0};

int matrix[25];

bool data_to_send=false;

void subsetup_fun(MQTT::MessageData& md){
    data_to_send=true;
}

void reset_game(){
    winner=-1;
    gameon=true;
    playing=0;
    attempts[0]=0, attempts[1]=0;
    hits[0]=0, hits[1]=0;
    for(int i=0; i<25; i++) matrix[i]=0;
    int counter=0;
    while(counter<3){ //random 1's in matrix
        int a=rand()%25;
        if(matrix[a]!=1) matrix[a]=1, counter++;
    }
}

void game_over(){
    if(hits[0]>hits[1]) winner=0;
    else if(hits[1]>hits[0]) winner=1;
    else winner=-1;
    
    gameon=false;
}

void guessing(MQTT::MessageData& md)
{
    if(gameon && !pause_game){
        MQTT::Message &message = md.message;
        printf("Mjesto %.*s\r\n", message.payloadlen, (char*)message.payload);
        int guessplace=atoi((char*)message.payload);
        if(guessplace<25 && guessplace>=0){
            if(matrix[guessplace]==1) hits[playing]++;
        }
        
        attempts[playing]++;
        playing=!playing;
        if(attempts[1]==10 || (hits[0]+hits[1])==3) game_over();
        if(attempts[0]==attempts[1]) pause_game=true; //refresh info every round
    }
}


int main(int argc, char* argv[])
{
    reset_game();
    printf("Ugradbeni sistemi\r\n");
    printf("SOBA 2: Igra pogadjanja\r\n\r\n");
    
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

    if ((rc = client.subscribe(SUBGUESS, MQTT::QOS2, guessing)) != 0)
        printf("rc from MQTT subscribe is %d\r\n", rc);
    
     if ((rc = client.subscribe(SUBSETUP, MQTT::QOS2, subsetup_fun)) != 0)//
        printf("rc from MQTT subscribe is %d\r\n", rc);

    MQTT::Message message;

    // QoS 0
    char buf[100];
    while(1) {
        if (!gameon) {
            if(winner!=-1) sprintf(buf, "{\"Pobjednik\": \"Pobjednik igrac: %d\"}", winner);
            else sprintf(buf, "{\"Nerijeseno\"}");
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBGAMEINFO, message);
            reset_game();
        }
        if(pause_game && gameon){
            sprintf(buf, "{\"Stanje\" \"Trenutno stanje: \"}");
            message.qos = MQTT::QOS0;
            message.retained = false;
            message.dup = false;
            message.payload = (void*)buf;
            message.payloadlen = strlen(buf);
            rc = client.publish(PUBGAMEINFO, message);
            int red=1;        
            for(int i=0;i<25;i+=5){
                sprintf(buf, "{\"Message\" \"Red %d: %d %d %d %d %d\"}", red++, matrix[i], matrix[i+1], matrix[i+2], matrix[i+3], matrix[i+4]);
                message.qos = MQTT::QOS0;
                message.retained = false;
                message.dup = false;
                message.payload = (void*)buf;
                message.payloadlen = strlen(buf);
                rc = client.publish(PUBGAMEINFO, message);
                wait(0.01);
            }
            sprintf(buf, "{\"Rezultat\": \"Igrac 0: %d pogodjenih | Igrac 1: %d pogodjenih\"}", hits[0], hits[1]);
                message.qos = MQTT::QOS0;
                message.retained = false;
                message.dup = false;
                message.payload = (void*)buf;
                message.payloadlen = strlen(buf);
                rc = client.publish(PUBGAMEINFO, message);
            pause_game=false;
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
        
        rc = client.subscribe(SUBSETUP, MQTT::QOS0, subsetup_fun);
        rc = client.subscribe(SUBGUESS, MQTT::QOS0, guessing);
        wait(1);
    }

}

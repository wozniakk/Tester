#!/bin/sh

TCP_SERVER_PORT=6790
TCP_REMOTE_PORT=6790

java -cp bin/:lib/sip.jar:lib/sbc.jar:lib/JMathStudio.jar local.tester.Tester $TCP_SERVER_PORT $1 $TCP_REMOTE_PORT -f config/config.cfg


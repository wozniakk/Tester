#!/bin/sh

TCP_SERVER_PORT=6790
TCP_REMOTE_ADDRESS='192.168.0.10'
TCP_REMOTE_PORT=6789

java -cp bin/:lib/sip.jar:lib/sbc.jar local.tester.Tester $TCP_SERVER_PORT $TCP_REMOTE_ADDRESS $TCP_REMOTE_PORT -f config/config.cfg


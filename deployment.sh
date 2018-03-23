#! /bin/bash

aws ec2 delete-key-pair --key-name LondonCircleKP --profile zsolt --region eu-west-2
privateKey=$(aws ec2 create-key-pair --key-name LondonCircleKP --profile zsolt --region eu-west-2 | jq '.KeyMaterial')
echo ${privateKey:1} > LondonCircleKP.pem
chmod 400 LondonCircleKP.pem
ssh -i LondonCircleKP.pem ec2-user@ec2-35-176-168-28.eu-west-2.compute.amazonaws.com
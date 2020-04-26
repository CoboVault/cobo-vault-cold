# Cobo Vault

Cobo Vault is an air-gapped & open source hardware wallet that uses completely transparent QR code data transmissions.Visit [Cobo Vault official website]( https://cobo.com/hardware-wallet/cobo-vault)  to know more information about Cobo Vault.

Follow [@Cobo Vault](https://twitter.com/CoboVault) on Twitter.

<div align=center><img src="https://cobo.com/_next/static/images/intro-2b5b0b44cc64639df4fcdd9ccc46fd4b.png"/></div>

## Contents

- [Introduction](#introduction)
- [Clone](#clone)
- [Build](#build)
- [Test](#test)
- [Code Structure](#code-structure)
- [Core Dependencies](#core-dependencies)
- [Issues and PRS](#issues-and-prs)
- [License](#license)


## Introduction
Cobo Vault runs as a standalone application on customized hardware and Android 8.1 Oreo (Go Edition).  This app performs:
1. Interaction with user. 
2. Interaction with mobile application [Cobo Vault Mobile](https://cobo.com/hardware-wallet/cobo-vault-app) via QR code. 
3. Interaction with Secure Element (SE) via serial port, the firmware of SE is opensourced at [cobo-vault-se-firmware](https://github.com/CoboVault/cobo-vault-se-firmware). The transaction data will be signed by this SE and the generated signature will be send back to this application. This signature and other necessary message will be displayed to user via QR code. Users use their mobile or desktop application to acquire signed transaction and broadcast it. 

The application of this hardware wallet is programmed with Java language. The transaction related work is done by Typescript opensourced at [crypto-coin-kit](https://github.com/CoboVault/crypto-coin-kit). The framework, J2V8 is used as the bridge between Java and Typescript. 


## Clone

    git clone git@github.com:CoboVault/cobo-vault-cold.git --recursive

## Build
    cd cobo-vault-cold
    ./gradlew assembleVault_v2Release
or you can build with IDEs, such as `Android Studio`,`intelliJ`

## Test
    ./gradlew test

## Code Structure
Modules:

`app` the main application module

`coinlib` the module for supported blockchains, currently included 12 blockchains

`encryption-core` module for Secure Element, include commands, protocol, serialize/deserialize, serial port communication

## Core Dependencies
1. [crypto-coin-message-protocol](https://github.com/CoboVault/crypto-coin-message-protocol) - protocol buffer of communication with mobile application
2. [crypto-coin-kit](https://github.com/CoboVault/crypto-coin-kit) - crypto-coin libraries
3. [cobo-vault-se-firmware](https://github.com/CoboVault/cobo-vault-se-firmware) - the firmware of SE

## Issues and PRS
any issues please submit at [issues](https://github.com/CoboVault/cobo-vault-cold/issues). and PRS are welcome!

## License
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-green.svg)](https://opensource.org/licenses/)
This project is licensed under the GPL License - see the [LICENSE](LICENSE) file for details

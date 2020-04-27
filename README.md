# Cobo Vault

Cobo Vault is an air-gapped, open source hardware wallet that uses completely transparent QR code data transmissions. Visit the [Cobo Vault official website]( https://cobo.com/hardware-wallet/cobo-vault)  to learn more about Cobo Vault.

You can also follow [@Cobo Vault](https://twitter.com/CoboVault) on Twitter.

<div align=center><img src="https://cobo.com/_next/static/images/intro-2b5b0b44cc64639df4fcdd9ccc46fd4b.png"/></div>

## Contents

- [Introduction](#introduction)
- [Clone](#clone)
- [Build](#build)
- [Test](#test)
- [Code Structure](#code-structure)
- [Core Dependencies](#core-dependencies)
- [Issues and PRs](#issues-and-prs)
- [License](#license)


## Introduction
Cobo Vault runs as a standalone application on customized hardware and Android 8.1 Oreo (Go Edition).  This app performs:
1. Interaction with the user. 
2. Interaction with the mobile application [Cobo Vault Mobile](https://cobo.com/hardware-wallet/cobo-vault-app) via QR code. 
3. Interaction with the Secure Element (SE) via serial port, open source SE firmware can be found at [cobo-vault-se-firmware](https://github.com/CoboVault/cobo-vault-se-firmware). Transaction data is signed by the Secure Element and the generated signature is sent back to the application. This signature and other necessary messages are displayed as a QR code. You can check the animation on our webpage to see the whole process. Users use their mobile or desktop application to acquire signed transaction data and broadcast it. 

The hardware wallet application was programmed with Java language. The transaction related work is done by Typescript, for which open source code is available at [crypto-coin-kit](https://github.com/CoboVault/crypto-coin-kit). The J2V8 framework is used as a bridge between Java and Typescript. 


## Clone

    git clone git@github.com:CoboVault/cobo-vault-cold.git --recursive

## Build
    cd cobo-vault-cold
    ./gradlew assembleVault_v2Release
You can also build with IDEs, such as `Android Studio`,`intelliJ`.

## Test
    ./gradlew test

## Code Structure
Modules

`app`: Main application module

`coinlib`: Module for supported blockchains, currently included in 12 blockchains

`encryption-core`: Module for the Secure Element, includes commands, protocol, serialize/deserialize, serial port communication

## Core Dependencies
1. [crypto-coin-message-protocol](https://github.com/CoboVault/crypto-coin-message-protocol) - protocol buffer of communication with the mobile application
2. [crypto-coin-kit](https://github.com/CoboVault/crypto-coin-kit) - crypto-coin libraries
3. [cobo-vault-se-firmware](https://github.com/CoboVault/cobo-vault-se-firmware) - Secure Element firmware

## Issues and PRs
Please submit any issues [here](https://github.com/CoboVault/cobo-vault-cold/issues). PRs are also welcome!

## License
[![GPLv3 License](https://img.shields.io/badge/License-GPL%20v3-green.svg)](https://opensource.org/licenses/)
This project is licensed under the GPL License. See the [LICENSE](LICENSE) file for details.

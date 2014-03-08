#!/usr/local/bin/python
from py4j.java_gateway import JavaGateway, GatewayClient

def main():
    gw = JavaGateway(GatewayClient(port=25346))

    print gw.entry_point.getWord('1245')
    print gw.entry_point.getWord('3245')
    print gw.entry_point.getWord('12345')
    print gw.entry_point.getWord('12485')
    print gw.entry_point.getWord('12745')

if __name__ == "__main__":
    main()

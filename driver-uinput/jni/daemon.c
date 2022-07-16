#include <arpa/inet.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <limits.h>
#include <stdint.h>
#include <unistd.h>

#include "protocol.h"

#define die(str, args...) { \
	perror(str); \
	exit(EXIT_FAILURE); \
}

int udp_socket;

int prepare_socket()
{
	int s;
	struct sockaddr_in addr;

	if ((s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP)) == -1)
		die("error: prepare_socket()");

	bzero(&addr, sizeof(struct sockaddr_in));
	addr.sin_family = AF_INET;
	addr.sin_port = htons(GFXTABLET_PORT);
	addr.sin_addr.s_addr = htonl(INADDR_ANY);

	if (bind(s, (struct sockaddr *)&addr, sizeof(addr)) == -1)
		die("error: prepare_socket()");

	return s;
}

int main(void)
{
	struct event_packet ev_pkt;
	udp_socket = prepare_socket();

	while (recv(udp_socket, &ev_pkt, sizeof(ev_pkt), 0) >= 9) {
		fflush(0);
		printf("%d %d %d %d %d %d\n", ntohs(ev_pkt.type), ntohs(ev_pkt.pressure), ntohs(ev_pkt.x), ntohs(ev_pkt.y), ntohs(ev_pkt.button), ntohs(ev_pkt.down));
	}

	close(udp_socket);


	return 0;
}

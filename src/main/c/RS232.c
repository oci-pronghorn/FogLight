#include <stdio.h>   /* Standard input/output definitions */
#include <string.h>  /* String function definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */

/*
 * 'open_port()' - Open serial port 1.
 *
 * Returns the file descriptor on success or -1 on error.
 */
int open_port() {
    int fd; /* File descriptor for the port */

    fd = open("/dev/ttys004", O_RDWR | O_NOCTTY | O_NDELAY);
    if (fd == -1) {
        perror("open_port: Unable to open /dev/ttyf1 - ");
    } else {
        fcntl(fd, F_SETFL, 0);

        // Get port options.
        struct termios options;
        tcgetattr(fd, &options);

        // Set I/O baud to 19,200.
        cfsetispeed(&options, B19200);
        cfsetospeed(&options, B19200);

        // Something about receivers and local modes.
        options.c_cflag |= (CLOCAL | CREAD);

        // Apply the options.
        tcsetattr(fd, TCSANOW, &options);
    }

    int n = write(fd, "ATZ\r", 4);
    if (n < 0) {
        printf("write() of 4 bytes failed!\n");
    }


    return (fd);
}

int main() {
    open_port();
    return 0;
}
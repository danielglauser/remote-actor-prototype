#!/bin/expect -f
spawn ssh caf@10.25.39.65 /home/caf/caf/bin/elastic-work-breakdown/bin/runCaf.sh
expect "*?assword:*"
send -- "caf\r"
# send blank line (\r) to make sure we get back to gui
send -- "\r"
expect eof
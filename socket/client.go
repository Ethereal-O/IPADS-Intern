package socket

import (
	"edgesystem/simulate"
	"encoding/json"
	"fmt"
	"log"
	"net"

	cron "github.com/robfig/cron/v3"
)

type Msg struct {
	PassengerNum []int
}

func RunSocketClient(url string, stations []simulate.Station, interval int) {
	conn, err := net.Dial("tcp", url)
	if err != nil {
		log.Fatal(err)
		return
	}

	crontab := cron.New(cron.WithSeconds())
	spec := "*/" + fmt.Sprintf("%d", interval) + " * * * * ?"
	_, err = crontab.AddFunc(spec, func() {
		var msg Msg
		for _, station := range stations {
			msg.PassengerNum = append(msg.PassengerNum, station.GetPassengers())
		}
		msgJSON, err := json.Marshal(msg)
		if err != nil {
			log.Fatal(err)
			return
		}
		_, err = conn.Write([]byte(msgJSON))
		if err != nil {
			log.Fatal(err)
		}
	})
	if err != nil {
		log.Fatal(err)
	}
	crontab.Start()
}

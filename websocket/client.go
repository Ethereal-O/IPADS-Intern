package ws

import (
	"fmt"
	"log"
	"encoding/json"
	"edgesystem/simulate"
	"github.com/gorilla/websocket"
	cron "github.com/robfig/cron/v3"
)

type Msg struct {
	passagerNum []int
}

func RunWebsocketClient(url string, stations []simulate.Station, interval int) {
	conn, _, err := websocket.DefaultDialer.Dial(url, nil)
	if err != nil {
		log.Fatal(err)
	}
	defer conn.Close()

	crontab := cron.New(cron.WithSeconds())
	spec := "*/" + fmt.Sprintf("%d", interval) + " * * * * ?";
	_, err = crontab.AddFunc(spec, func() {
		var msg Msg
		for _, station := range stations {
			msg.passagerNum = append(msg.passagerNum, station.GetPassagers())
		}
		msgJSON, err := json.Marshal(msg)
		if err != nil {
			log.Fatal(err)
			return
		}
		err = conn.WriteMessage(websocket.TextMessage, msgJSON)
		if err != nil {
			log.Fatal(err)
		}
	})
	if err != nil {
		log.Fatal(err)
	}
	crontab.Start()
}
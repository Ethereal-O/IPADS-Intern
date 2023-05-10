package simulate

import (
	"fmt"
	"log"
	"edgesystem/simulate/guass"
	cron "github.com/robfig/cron/v3"
)

type Station struct {
	Id             int
	Max            int
	Min            int
	Mean           int
	Std            int
	NumOfPassagers int
	crontab		   *cron.Cron
	status         bool
}

func NewStation(id int, max int, min int, mean int, std int) *Station {
	return &Station{
		Id:             id,
		Max:            max,
		Min:            min,
		Mean:           mean,
		Std:            std,
		NumOfPassagers: 0,
		crontab:		cron.New(cron.WithSeconds()),
		status:			false,
	}
}

func (s *Station) GetPassagers() int {
	return s.NumOfPassagers
}

func (s *Station) SetMin(min int) {
	s.Min = min
}

func (s *Station) SetMax(max int) {
	s.Max = max
}

func (s *Station) SetMean(mean int) {
	s.Mean = mean
}

func (s *Station) SetStd(std int) {
	s.Std = std
}

func (s *Station) SetNumOfPassagers(num int) {
	s.NumOfPassagers = num
}

func (s *Station) Simulate(interval int) {
	if s.status {
		log.Println("Station", s.Id, "is simulating")
		return
	}
	spec := "*/" + fmt.Sprintf("%d", interval) + " * * * * ?";
	_, err := s.crontab.AddFunc(spec, func() {
		numChange := guass.RandomNormal(s.Mean, s.Std, s.Min, s.Max)
		if s.NumOfPassagers + numChange < 0 {
			s.NumOfPassagers = 0
		} else {
			s.NumOfPassagers += numChange
		}
	})
	if err != nil {
		log.Fatal(err)
		return
	}
	s.crontab.Start()
	s.status = true
}

func (s *Station) Stop() {
	if !s.status {
		fmt.Println("Station", s.Id, "is not simulating")
		return
	}
	s.crontab.Stop()
	s.status = false
}
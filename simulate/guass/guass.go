package guass

import (
	"math"
	"math/rand"
	"time"
)

func Normal(x float64, mean float64, std float64) float64 {
	return math.Exp(-math.Pow(x-mean, 2)/(2*math.Pow(std, 2))) / (std * math.Sqrt(2*math.Pi))
}

func RandomUniform(min float64, max float64) float64 {
	rand.Seed(time.Now().UnixNano())
	return min + float64(rand.Intn(100000)) * (max - min) / 100000.0
}

func RandomNormal(mean int, std int, min int, max int) int {
	rand.Seed(time.Now().UnixNano())
	for {
		x := RandomUniform(float64(min), float64(max))
		y := Normal(float64(x), float64(mean), float64(std))
		dScope := RandomUniform(0, Normal(float64(mean), float64(mean), float64(std)))
		if dScope < y {
			return int(x)
		}
	}
}

func main() {
	var testData = []int{}
	for i := 1; i < 100; i++ {
		xx := RandomNormal(80, 10, 0, 100)
		testData = append(testData, xx)
	}

	var sum int = 0
	for _, v := range testData {
		sum += v
	}
	println("平均值：", sum / int(len(testData)))
}
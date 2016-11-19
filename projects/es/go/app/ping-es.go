package main

import (
  elastic "gopkg.in/olivere/elastic.v5"
  "fmt"
)

func main() {

  client, err := elastic.NewClient(
    elastic.SetURL("http://localhost:80"),
    elastic.SetMaxRetries(10),
    elastic.SetBasicAuth("powerjack", "XXXXXX"))
  if err != nil {
    panic(err)
  }
  fmt.Println(client.String())
  fmt.Println(client.IsRunning())
  
  info, err := client.ElasticsearchVersion("http://localhost:80")
  if err != nil {
    panic(err)
  }
  fmt.Printf("Elasticsearch returned version %s\n", info)

  fmt.Println("Done")
}

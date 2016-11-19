package main

// http://olivere.github.io/elastic/

import (
  "encoding/csv"
  "encoding/json"
  "fmt"
  "os"
  elastic "gopkg.in/olivere/elastic.v5"
)

type Person struct {
  Id string
  FName string
  LName string
  Email string
  Gender string
  IPAddr string
}

func main() {
  fmt.Println("Start")
  csvFile, err := os.Open("./data.tsv")
  if err != nil {
    fmt.Println(err)
    os.Exit(1)
  }
  defer csvFile.Close()
  reader := csv.NewReader(csvFile)
  reader.Comma = '\t'
  reader.FieldsPerRecord = -1
  csvData, err := reader.ReadAll()
  if err != nil {
    fmt.Println(err)
    os.Exit(1)
  }
  var oneRecord Person
  var allRecords []Person
  for _, each := range csvData {
    oneRecord.Id = each[0]
    oneRecord.FName = each[1]
    oneRecord.LName = each[2]
    oneRecord.Email = each[3]
    oneRecord.Gender = each[4]
    oneRecord.IPAddr = each[5]
    allRecords = append(allRecords, oneRecord)
  }
  jsondata, err := json.Marshal(allRecords) // convert to JSON
  if err != nil {
    fmt.Println(err)
    os.Exit(1)
  }
  // sanity check
  // NOTE : You can stream the JSON data to http service as well instead of saving to file
  //fmt.Println(string(jsondata))

  client, err := elastic.NewClient()
  if err != null {
    fmt.Println(err)
    os.Exit(1)
  }

  // now write to JSON file
  jsonFile, err := os.Create("./data.json")
  if err != nil {
    fmt.Println(err)
  }
  defer jsonFile.Close()

  jsonFile.Write(jsondata)
  jsonFile.Close()

  fmt.Println("Done")  
}

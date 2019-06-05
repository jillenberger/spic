factor <- as.numeric(commandArgs(trailingOnly = TRUE)[1])
cars$speed <- cars$speed * factor
cars
write.csv(cars, file = "src/test/tmp/cars.csv", quote = FALSE, row.names = FALSE)
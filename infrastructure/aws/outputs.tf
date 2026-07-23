output "ecr_repository_url" {
  value = aws_ecr_repository.app.repository_url
}

output "load_balancer_url" {
  value = "http://${aws_lb.app.dns_name}"
}

output "rds_endpoint" {
  value     = aws_db_instance.mysql.endpoint
  sensitive = true
}

output "assets_bucket" {
  value = aws_s3_bucket.assets.id
}

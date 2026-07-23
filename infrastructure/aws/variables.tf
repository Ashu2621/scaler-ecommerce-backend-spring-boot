variable "aws_region" {
  type    = string
  default = "ap-south-1"
}

variable "project_name" {
  type    = string
  default = "scaler-commerce"
}

variable "environment" {
  type    = string
  default = "prod"
}

variable "container_image_tag" {
  type    = string
  default = "latest"
}

variable "jwt_secret_arn" {
  description = "Secrets Manager ARN containing the Base64 JWT secret."
  type        = string
  sensitive   = true
}

variable "route53_zone_id" {
  description = "Optional existing Route53 hosted zone ID."
  type        = string
  default     = ""
}

variable "domain_name" {
  description = "Optional API DNS name, for example api.example.com."
  type        = string
  default     = ""
}

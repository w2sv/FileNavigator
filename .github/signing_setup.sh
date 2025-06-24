#!/bin/bash

# taken from https://github.com/PaulWoitaschek/Voice/blob/main/ci_signing_setup.sh
decode_env_to_file() {
  local env_var="${1}"
  local dest_file="${2}"
  if [[ -n "${!env_var}" ]]; then
    if [[ ! -f "${dest_file}" ]]; then
      echo "${!env_var}" | base64 --decode >"${dest_file}"
      echo "Success: Written to ${dest_file}"
    else
      echo "Warning: File ${dest_file} already exists, not overwritten."
    fi
  else
    echo "Warning: Environment variable ${env_var} is empty or not set, no action taken."
  fi
}

decode_env_to_file "SIGNING_KEYSTORE" "../keys.jks"
decode_env_to_file "SIGNING_PROPERTIES" "../keys.properties"

wget https://github.com/facebook/ktfmt/releases/download/v0.52/ktfmt-0.52-jar-with-dependencies.jar -O $HOME/ktfmt-0.52-jar-with-dependencies.jar
touch $HOME/.bashrc
echo """
function ktfmt {
  file=\"\$HOME/ktfmt-0.52-jar-with-dependencies.jar\"
  if test -f \"\$file\" ; then
    if [ -n \"\$1\" ]; then
      java -jar \$file --google-style \"\$1\"
    else
      echo \"Usage: formatkt <path-to-file-or-directory>\"
    fi
  else
    echo \"Unable to format\"
  fi
}

export -f ktfmt
""" >> $HOME/.bashrc

source $HOME/.bashrc
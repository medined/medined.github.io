# Use the official Ruby image as the base image
FROM ruby:3.0

# Install dependencies
RUN apt-get update --no-install-recommends -qq \
 && apt-get install --no-install-recommends -y build-essential=12.9 libpq-dev=13.15-0+deb11u1 nodejs=12.22.12~dfsg-1~deb11u4 \
 && apt-get clean \
 && rm -rf /var/lib/apt/lists/*

# Set the working directory in the container
WORKDIR /usr/src/app

# Copy the Gemfile and Gemfile.lock into the container
COPY Gemfile Gemfile.lock ./

# Install the necessary gems
RUN gem install -v bundler '2.5.14' && bundle install

# Copy the rest of the application code into the container
COPY . .

# Expose the port that Jekyll will serve on
EXPOSE 4000

# Add HEALTHCHECK instruction
HEALTHCHECK CMD curl --fail http://localhost:4000 || exit 1

# Create a non-root user
RUN useradd -m myuser

# Set the user for the container
USER myuser

# Run Jekyll server
CMD ["bundle", "exec", "jekyll", "serve", "--host", "0.0.0.0"]

MAPS = 1..200
BOTS = Dir.glob(File.join "example_bots", "*.jar").map { |jar| jar.scan(/example_bots\/(.*)Bot.jar/) }.flatten.sort

def parse_results(output)
  output = output.split("\n")
  loss = (output[-1] =~ /Player 1 Wins/).nil?
  turns = output[-2].split[1]
  [!loss, turns.to_i]
end

def print_results(victory, turns)
  print victory ? "W" : "L"
  print "(#{turns})"
  STDOUT.flush
  puts
end

def save_losses(losing_matches)
  File.open('losing_matches.txt', 'w') do |file|
    losing_matches.each do |bot, map|
  	  file.write "#{bot} #{map}\n"
  	end
  end
end

def play_game(map, challenger)
  my_cmd = 'java -cp build/ MyBot'
  opp_cmd = "java -jar example_bots/#{challenger}Bot.jar"

  cmd = %Q{ENVIRONMENT=test java -jar tools/PlayGame.jar maps/map#{map}.txt 1000 200 log.txt "#{my_cmd}" "#{opp_cmd}"}
  `#{cmd} 2> commentary.txt > video.txt`
  parse_results(File.read('commentary.txt'))
end

task :build do
  `ant`
  raise "build failed" unless $? == 0
end

task :play => :build do
  map = ENV['MAP'] || 7
  bot = ENV['BOT'] || 'Dual'
  print_results *play_game(map, bot)
end

task :replay_losses => :build do
  losing_matches = []
  IO.readlines('losing_matches.txt').each do |line|
  	break if line.strip.empty?
    bot, map = line.split(" ")
	print "Challenging #{bot}Bot on map#{map}: "
	victory, turns = play_game map, bot
	print_results victory, turns
	losing_matches << [bot, map] unless victory
  end
end

task :watch do
  `cat video.txt | java -jar tools/ShowGame.jar`
end

task :tournament => :build do
  losing_matches = []
  bots = ENV['BOTS'] ? ENV['BOTS'].gsub(" ", "").split(",") : BOTS
  bots = ENV['BOT'] ? [ENV['BOT']] : bots
  maps = ENV['MAPS'] ? ENV['MAPS'].gsub(" ", "").split(",") : MAPS
  maps = ENV['MAP'] ? [ENV['MAP']] : maps
  bots.reverse.each do |bot|
    wins = losses = turns_sum = 0
    print "Challenging #{bot}Bot: "
    maps.each do |map|
      victory, turns = play_game map, bot
      turns_sum += turns
	  print (victory ? 'W' : 'L')
	  STDOUT.flush
	  victory ? wins += 1 : losses +=1
	  losing_matches << [bot, map] unless victory
    end
    puts "  #{wins}-#{losses} (avg #{turns_sum/(wins+losses)} turns)"
  end
  save_losses(losing_matches)
end

task :help do
  puts "Available bots: #{BOTS.join(',')}"
  puts "Available maps: #{MAPS.inspect}"
end

desc "Runs the bot against the dhartmei's tcp server"
task :run_tcp_contest do
  runs = ENV['RUNS'].to_i || 100
  runs.times do
    puts `./tcp/tcp 72.44.46.68 995 ${BOT} -p x${BOT}1q2w ./tcp/MyBot >> log/${BOT}`
  end
end

task :run_all_bots do
  runs = 100
  bots = BOTS.grep(/Luda/)
  runs.times do
    bots.each do |bot|
      puts `export BOT=#{bot}; ./tcp/tcp 72.44.46.68 995 #{bot} -p x#{bot}1q2w ./tcp/MyBot >> log/#{bot}`
    end
  end
end

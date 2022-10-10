TARGET		:=	launcher

# TODO Keep this updated until we automate it.
VERSION_MAJOR	:=	1
VERSION_MINOR	:=	0
VERSION_PATCH	:=	0

# Build directories
DIR_SRC		:=	source
DIR_INC		:=	include
DIR_BLD		:=	build
DIR_OUT		:=	out
DIR_JDK		:=	/lib/jvm/default

# Required libraries and include dirs.
LIBS		:=	dl
INCLUDES	:=	$(DIR_JDK)/include $(DIR_JDK)/include/linux

FLAGS_COM	:=	-Wall -Wextra -Werror -ggdb -pthread
FLAGS_CCOM	:=	$(FLAGS_COM) \
				-c -DVERSION_MAJOR=$(VERSION_MAJOR) -DVERSION_MINOR=$(VERSION_MINOR) -DVERSION_PATCH=$(VERSION_PATCH) \
				$(addprefix -I,$(DIR_INC) $(INCLUDES))
FLAGS_CC	:=	$(FLAGS_CCOM) \
				-std=c17
FLAGS_CXX	:=	$(FLAGS_CCOM) \
				-std=c++20 -fno-exceptions -fno-rtti
FLAGS_LD	:=	$(FLAGS_COM) \
				$(addprefix -l, $(LIBS))

RWILDCARD	=	$(foreach d,$(wildcard $(1:=/*)),$(call RWILDCARD,$d,$2) $(filter $(subst *,%,$2),$d))

FILES_C		:=	$(call RWILDCARD,$(DIR_SRC),*.c)
FILES_CXX	:=	$(call RWILDCARD,$(DIR_SRC),*.cpp)
FILES_H		:=	$(call RWILDCARD,$(DIR_INC),*.h) \
				$(call RWILDCARD,$(DIR_INC),*.hpp)
FILES_O		:=	$(patsubst $(DIR_SRC)/%.c,$(DIR_BLD)/%.c.o,$(FILES_C)) \
				$(patsubst $(DIR_SRC)/%.cpp,$(DIR_BLD)/%.cpp.o,$(FILES_CXX))

PREFIX		:=
CC			:=	$(PREFIX)clang
CXX			:=	$(PREFIX)clang++
LD			:=	$(PREFIX)clang

all: $(DIR_OUT)/$(TARGET)

run: all
	$(DIR_OUT)/$(TARGET)

clean:
	rm -vf $(DIR_OUT)/$(TARGET) $(FILES_O)

$(DIR_OUT)/$(TARGET): $(FILES_O)
	@mkdir -p $(@D)
	$(LD) $^ $(FLAGS_LD) -o $@

$(DIR_BLD)/%.c.o: $(DIR_SRC)/%.c Makefile $(FILES_H)
	@mkdir -p $(@D)
	$(CC) $(FLAGS_CC) -o $@ $<

$(DIR_BLD)/%.cpp.o: $(DIR_SRC)/%.cpp Makefile $(FILES_H)
	@mkdir -p $(@D)
	$(CXX) $(FLAGS_CXX) -o $@ $<
